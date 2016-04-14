package com.getfsc.retroserver;

import com.getfsc.retroserver.http.ServerRequest;
import com.getfsc.retroserver.http.ServerResponse;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 下午12:14
 */
public class DirectCall<T> implements Call<T> {

    private Consumer<ServerRequest> func;
    private ServerRequest request;
    private boolean isExecuted=false;



    public DirectCall(Consumer<ServerRequest> func) {
        this.func = func;
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
            func.accept(request);
            return null; // we don't need Retrofit Response
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
        return new DirectCall<>(func);
    }

    @Override
    public Request request() {
        return null;
    }
}
