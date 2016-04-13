package com.getfsc.retroserver;

import com.getfsc.retroserver.request.ServerRequest;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 下午12:14
 */
public class DirectCall<T> implements Call<T> {

    private Codeblock<T> codeblock;
    private ServerRequest request;
    private boolean isExecuted=false;

    @FunctionalInterface
    public interface Codeblock<G> {
        public Response<G> call(ServerRequest request);
    }

    public DirectCall(Codeblock<T> codeblock) {
        this.codeblock = codeblock;
    }

    public ServerRequest getRequest() {
        return request;
    }

    public void setRequest(ServerRequest request) {
        this.request = request;
    }

    @Override
    public Response<T> execute()  {
        try {
            return codeblock.call(request);
        } finally {
            isExecuted = true;
        }
    }

    @Override
    public void enqueue(Callback<T> callback) {
    }

    @Override
    public boolean isExecuted() {
        return isExecuted;
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
        return new DirectCall<>(codeblock);
    }

    @Override
    public Request request() {
        return null;
    }
}
