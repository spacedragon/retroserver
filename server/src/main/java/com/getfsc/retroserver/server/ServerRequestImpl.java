package com.getfsc.retroserver.server;

import com.getfsc.retroserver.ObjectConvert;
import com.getfsc.retroserver.Route;
import com.getfsc.retroserver.request.ServerRequest;
import com.getfsc.retroserver.request.Value;
import com.getfsc.retroserver.util.H;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.router.RouteResult;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.net.HttpHeaders.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 上午1:01
 */
public class ServerRequestImpl implements ServerRequest {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(NettyServer.class);
    private static final int HTTP_CACHE_SECONDS = 315360000;

    private final HttpRequest request;
    private final HttpPostRequestDecoder decoder;
    private CompositeByteBuf bodyBuf;
    private final RouteResult<Route> routeResult;

    public ServerRequestImpl(HttpRequest request, HttpPostRequestDecoder decoder, CompositeByteBuf bodyBuf, RouteResult<Route> routeResult) {

        this.request = request;
        this.decoder = decoder;
        this.bodyBuf = bodyBuf;
        this.routeResult = routeResult;
        if (bodyBuf != null)
            bodyBuf.retain();
    }

    private HashMap<String, HttpData> data = new HashMap<>();

    private Map<String, io.netty.handler.codec.http.multipart.HttpData> form() {
        while (decoder.hasNext()) {
            InterfaceHttpData httpData = decoder.next();
            switch (httpData.getHttpDataType()) {
                case Attribute:
                    Attribute attribute = (Attribute) httpData;
                    data.put(httpData.getName(), attribute);
                    break;
                case FileUpload:
                    data.put(httpData.getName(), (FileUpload) httpData);
            }
        }
        return data;
    }

    @Override
    public Value path(String key) {
        return () -> routeResult.param(key);
    }

    @Override
    public Value field(String key) {
        return () -> {
            try {
                return form().get(key).getString();
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public Value query(String key) {
        return () -> routeResult.queryParam(key);
    }

    @Override
    public RequestBody part(String key) {
        HttpData httpData = form().get(key);
        if (httpData instanceof FileUpload)
            return new UploadFileRequestBody((FileUpload) httpData);
        else {
            return new AttributeRequestBody(httpData);
        }
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public String verb() {
        return request.method().toString();
    }

    @Override
    public <T> T body(Class<T> clz) {
        return ObjectConvert.convert(bodyBuf.toString(Charset.forName("utf8")), clz);
    }

    @Override
    public Map<String, String> queryMap() {
        return routeResult.queryParams().keySet().stream()
                .collect(Collectors.toMap(Function.identity(), routeResult::queryParam));
    }

    @Override
    public Value header(String key) {
        return () -> request.headers().get(key);
    }

    public void destroy() {
        if (bodyBuf != null) {
            bodyBuf.release();
        }
        if (decoder != null) {
            decoder.destroy();
        }
    }

    public void handleResponse(Response r, ChannelHandlerContext ctx) {
        // Build the response object.

        try {

            Headers.Builder headerBuilder = r.headers().newBuilder();
            routeResult.target().getHeaders().forEach(headerBuilder::add);
            Headers headers = headerBuilder.build();
            String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE.toString());
            MediaType mediaType = contentType == null ? null : MediaType.parse(contentType);
            String tt = mediaType == null ? "json" : mediaType.type();
            switch (tt) {
                case "application":
                case "json":
                    handleJson(r.code(), headers, r.body(), ctx);
                    break;
                case "text":
                    handleText(r.code(), headers, r.body().toString(), mediaType.charset(), ctx);
                    break;
                case "audio":
                case "video":
                case "image":
                default:
                    handleFile(r.code(), headers, r.body(), ctx);
            }

        } catch (Throwable e) {
            handleError(ctx, e);
        }


    }

    private void handleText(int code, Headers headers, String body, Charset charset, ChannelHandlerContext ctx) {
        writeResponse(code, headers, Unpooled.wrappedBuffer(body.getBytes(charset)), ctx);
    }

    private void handleJson(int code, Headers headers, Object body, ChannelHandlerContext ctx) {
        byte[] json = ObjectConvert.toJson(body);
        writeResponse(code, headers, Unpooled.wrappedBuffer(json), ctx);
    }

    private void handleFile(int code, Headers headers, Object body, ChannelHandlerContext ctx) throws Exception {
        if (body instanceof File) {
            File file = (File) body;
            writeFileResponse(code, headers, file, ctx);

        } else if (body instanceof byte[]) {
            byte[] bytes = (byte[]) body;
            writeResponse(code, headers, Unpooled.copiedBuffer(bytes), ctx);
        } else if (body instanceof ByteBuf) {
            writeResponse(code, headers, Unpooled.copiedBuffer((ByteBuf) body), ctx);
        } else if (body instanceof ByteBuffer) {
            writeResponse(code, headers, Unpooled.copiedBuffer((ByteBuffer) body), ctx);
        } else {
            throw H.rte("unknown file object type:" + body.getClass().getCanonicalName());
        }
    }

    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

    private static void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);
        setDateHeader(response);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));
    }

    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
                LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    private void writeFileResponse(int code, Headers headers, File file, ChannelHandlerContext ctx) throws ParseException, IOException {
        String ifModifiedSince = request.headers().getAsString(IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx);
                return;
            }
        }
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, fileLength);
        setDateAndCacheHeaders(response, file);
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // Write the content.
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            sendFileFuture =
                    ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            // Write the end marker.
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            sendFileFuture =
                    ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                            ctx.newProgressivePromise());
            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;
        }

        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) { // total unknown
                    log.debug(future.channel() + " Transfer progress: " + progress);
                } else {
                    log.debug(future.channel() + " Transfer progress: " + progress + " / " + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                log.debug(future.channel() + " Transfer complete.");
            }
        });

        // Decide whether to close the connection or not.
        if (!HttpUtil.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

    }

    private void writeResponse(int code, Headers headers, ByteBuf buffer, ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, HttpResponseStatus.valueOf(code), buffer);

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        HttpHeaders headersWriting = response.headers();
        headers.toMultimap().forEach(headersWriting::add);
        if (keepAlive) {
            headersWriting.setInt(CONTENT_LENGTH, response.content().readableBytes());
            headersWriting.set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Encode the cookie.
        String cookieString = this.request.headers().getAsString(COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (Cookie cookie : cookies) {
                    headersWriting.add(SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
                }
            }
        }
        ctx.write(response);

        if (!keepAlive) {
            // If keep-alive is off, close the connection once the content is fully written.
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void handleError(ChannelHandlerContext ctx, Throwable e) {
        log.error(e);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer("Failure: " + e.getMessage() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
