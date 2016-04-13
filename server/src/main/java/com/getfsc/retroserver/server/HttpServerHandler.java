/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.getfsc.retroserver.server;

import com.getfsc.retroserver.AsyncCall;
import com.getfsc.retroserver.DirectCall;
import com.getfsc.retroserver.Route;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.router.RouteResult;
import io.netty.handler.codec.http.router.Router;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Set;

import static com.google.common.net.HttpHeaders.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(HttpServerHandler.class);


    private HttpRequest request;
    private Router<Route> router;
    private RouteResult<Route> routeResult;
    private HttpPostRequestDecoder decoder;
    private CompositeByteBuf contentBuffer;


    private void reset() {
        routeResult = null;
        if(contentBuffer!=null)
            contentBuffer.release();
        contentBuffer = null;

        decoder = null;
        request = null;
    }

    public HttpServerHandler(Router<Route> router) {
        this.router = router;
    }

    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            routeResult = router.route(request.method(), request.uri());
            if (request.method().equals(HttpMethod.POST) || request.method().equals(HttpMethod.PUT)) {
                switch (routeResult.target().getBodyType()) {
                    case FORM_URL_ENCODED:
                    case MULTIPART:
                        decoder = new HttpPostRequestDecoder(factory, request);
                        break;
                    default:
                        contentBuffer = ctx.alloc().compositeBuffer();
                }
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            if (decoder != null) {
                try {
                    decoder.offer(httpContent);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    log.error(e1);
                    badRequest(ctx);
                    return;
                }
            } else if (contentBuffer != null) {
                contentBuffer.addComponent(httpContent.content());
            }
            // readHttpDataChunkByChunk();

            if (msg instanceof LastHttpContent) {
                if (routeResult.target() == Route.NotFound) {
                    sendNotFound(ctx);
                    reset();
                } else {

                    ServerRequestImpl req = new ServerRequestImpl(request, decoder, contentBuffer, routeResult);
                    Call call = routeResult.target().getCaller().call(req);
                    if (call instanceof DirectCall) {
                        DirectCall directCall = (DirectCall) call;
                        directCall.setRequest(req);

                        try {
                            req.handleResponse(directCall.execute(), ctx);
                        } finally {
                            req.destroy();
                            reset();
                        }

                    } else {
                        call.enqueue(new Callback() {
                            private final ChannelHandlerContext context = ctx;

                            @Override
                            public void onResponse(Call call, Response response) {
                                try {
                                    req.handleResponse(response, context);
                                } finally {
                                    req.destroy();
                                }
                            }

                            @Override
                            public void onFailure(Call call, Throwable t) {
                                try {
                                    req.handleError(context, t);
                                } finally {
                                    req.destroy();
                                }
                            }
                        });
                        reset();
                    }
                }
            }
        }
    }

    private void sendNotFound(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, NOT_FOUND, Unpooled.copiedBuffer("Failure: " + NOT_FOUND + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    private void badRequest(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
        ctx.write(response);
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        reset();
    }


    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause);
        ctx.close();
        reset();
    }
}