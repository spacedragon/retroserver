package com.getfsc.retroserver.server;

import com.getfsc.retroserver.http.ServerResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 下午6:01
 */
public class ServerResponseImpl implements ServerResponse {
    protected DefaultHttpResponse rawResponse;
    private Object body;

    public ServerResponseImpl(DefaultHttpResponse rawResponse) {
        this.rawResponse = rawResponse;
    }

    @Override
    public int code() {
        return rawResponse.status().code();
    }

    @Override
    public ServerResponse code(int code) {
        rawResponse.setStatus(HttpResponseStatus.valueOf(code));
        return this;
    }

    @Override
    public ServerResponse addHeader(String head, String value) {
        rawResponse.headers().add(head, value);
        return this;
    }

    @Override
    public ServerResponse setHeader(String head, String value) {
        rawResponse.headers().set(head, value);
        return this;
    }

    @Override
    public String header(String header) {
        return rawResponse.headers().getAsString(header);
    }

    @Override
    public boolean containsHeader(String header) {
        return rawResponse.headers().contains(header);
    }

    @Override
    public Object body() {
        return body;
    }

    @Override
    public ServerResponse setBody(Object body) {
        this.body = body;
        return this;
    }




}
