package com.getfsc.retroserver.server;

import io.netty.handler.codec.http.multipart.HttpData;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 上午11:13
 */
public class AttributeRequestBody extends RequestBody {
    private HttpData httpData;

    public AttributeRequestBody(HttpData httpData) {
        this.httpData = httpData;
    }

    @Override
    public MediaType contentType() {
        return null;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        sink.write(httpData.get());
    }
}
