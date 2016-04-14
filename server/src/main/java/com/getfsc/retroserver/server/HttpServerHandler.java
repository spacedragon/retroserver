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

import com.getfsc.retroserver.DirectCall;
import com.getfsc.retroserver.Route;
import com.getfsc.retroserver.aop.AopFactory;
import com.getfsc.retroserver.aop.AopInterceptor;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.router.RouteResult;
import io.netty.handler.codec.http.router.Router;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import okhttp3.Protocol;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.stream.Collectors;

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
        if (contentBuffer != null)
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
                Route route = routeResult.target();

                if (route == Route.NotFound) {
                    sendNotFound(ctx);
                    reset();
                } else {

                    ServerRequestImpl req = new ServerRequestImpl(request, decoder, contentBuffer, routeResult);
                    try {
                        List<AopInterceptor> aops = route.getAopFactories().stream().map(AopFactory::create)
                                .collect(Collectors.toList());
                        Call call = null;
                        if (aops.stream().allMatch(aop -> aop.beforeInvoke(req))) {
                            call = route.getCaller().call(req);
                        }
                        if (call == null) {
                            okhttp3.Response.Builder respBuilder = new okhttp3.Response.Builder()
                                    .protocol(Protocol.HTTP_1_1)
                                    .request(new Request.Builder().url("http://localhost/").build());
                            final okhttp3.Response finalResponse = aopResponse(aops,req, respBuilder);
                            if (finalResponse.code() == -1) {
                                sendNotFound(ctx);
                            } else {
                                req.handleResponse(finalResponse, finalResponse.body(), ctx);
                                reset();
                            }
                            aops.forEach(AopInterceptor::destory);
                        } else if (call instanceof DirectCall) {
                            DirectCall directCall = (DirectCall) call;
                            directCall.setRequest(req);

                            try {
                                Response r = directCall.execute();
                                okhttp3.Response.Builder respBuilder = r.raw().newBuilder();
                                Object body=r.body();
                                okhttp3.Response finalResponse = aopResponse(aops, req,respBuilder);
                                req.handleResponse(finalResponse,body, ctx);
                            } finally {
                                req.destroy();
                                aops.forEach(AopInterceptor::destory);
                                reset();
                            }

                        } else {
                            call.enqueue(new Callback() {
                                private final ChannelHandlerContext context = ctx;

                                @Override
                                public void onResponse(Call call, Response response) {
                                    try {
                                        okhttp3.Response.Builder respBuilder = response.raw().newBuilder();
                                        okhttp3.Response finalResponse = aopResponse(aops,req, respBuilder);
                                        req.handleResponse(finalResponse, response.body(), context);
                                    } finally {
                                        req.destroy();
                                        aops.forEach(AopInterceptor::destory);
                                    }
                                }

                                @Override
                                public void onFailure(Call call, Throwable t) {
                                    try {
                                        req.handleError(context, t);
                                    } finally {
                                        req.destroy();
                                        aops.forEach(AopInterceptor::destory);

                                    }
                                }
                            });
                            reset();
                        }
                    } catch (Exception e) {
                        req.handleError(ctx, e);
                        reset();
                    }
                }
            }
        }
    }

    private okhttp3.Response aopResponse(List<AopInterceptor> aops, ServerRequestImpl req, okhttp3.Response.Builder resp) {
        for (AopInterceptor aop : aops) {
            resp = aop.afterInvoke(req,resp);
        }
        return resp.build();
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
