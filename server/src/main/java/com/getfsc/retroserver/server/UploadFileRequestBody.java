package com.getfsc.retroserver.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 上午2:29
 */
class UploadFileRequestBody extends RequestBody {
    private FileUpload fileUpload;

    UploadFileRequestBody(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    @Override
    public MediaType contentType() {
            return MediaType.parse(fileUpload.getContentType());
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
            Source source;
            if (fileUpload.isInMemory()) {
                source = Okio.source(new ByteBufInputStream(fileUpload.getByteBuf()));
            } else {
                source = Okio.source(fileUpload.getFile());
            }
            sink.writeAll(source);
    }
}
