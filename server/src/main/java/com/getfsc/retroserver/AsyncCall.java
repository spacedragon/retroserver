package com.getfsc.retroserver;

import com.getfsc.retroserver.request.ServerRequest;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 上午11:55
 */
public class AsyncCall<T> implements Call<T> {

    private Codeblock<T> codeblock;

    @FunctionalInterface
    public interface Codeblock<G> {
        void run(ServerRequest request, Callback<G> callback);
    }


    public AsyncCall(Codeblock<T> codeblock) {
        this.codeblock = codeblock;
    }

    @Override
    public Response<T> execute() throws IOException {
        throw new RuntimeException("should not be here");
    }

    @Override
    public void enqueue(Callback<T> callback) {
        codeblock.run(request,callback);
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return new AsyncCall<>(codeblock);
    }

    ServerRequest request;

    public ServerRequest getRequest() {
        return request;
    }

    public void setRequest(ServerRequest request) {
        this.request = request;
    }

    @Override
    public Request request() {
        throw new RuntimeException("don't use this");
    }
}
