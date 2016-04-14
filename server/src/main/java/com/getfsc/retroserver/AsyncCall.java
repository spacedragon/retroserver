package com.getfsc.retroserver;

import com.getfsc.retroserver.http.ServerRequest;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 上午11:55
 */
public class AsyncCall<T> implements Call<T> {

    private BiConsumer<ServerRequest, Callback<T>> func;


    public AsyncCall(BiConsumer<ServerRequest, Callback<T>> func) {
        this.func = func;
    }


    public void executeAsync(Callback<T> callback) {
        func.accept(request, callback);
    }


    @Override
    public Response<T> execute() throws IOException {
        throw new RuntimeException("should not be here");
    }

    @Override
    public void enqueue(retrofit2.Callback<T> callback) {
        throw new RuntimeException("should not be here");
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
        return new AsyncCall<>(func);
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
