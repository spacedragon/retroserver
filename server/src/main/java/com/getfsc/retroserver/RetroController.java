package com.getfsc.retroserver;

import com.getfsc.retroserver.http.ServerRequest;
import retrofit2.Call;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/10
 * Time: 下午12:01
 */
public class RetroController {

    protected <T> Call<T> ok(T result) {
        return new DirectCall<>(request -> {
            request.response().ok(result);
        });
    }



    protected <T> Call<T> error(int code, T result) {
        return new DirectCall<>(request -> request.response()
                .code(code)
                .setBody(result));
    }

    protected <T> Call<T> notfound(T result) {
        return error(404, result);
    }
    protected <T> Call<T> redirect(String url) {
        return new DirectCall<>(request -> request.response()
                .redirect(url, null));
    }


    public <T> Call<T> execute(Consumer<ServerRequest> function) {
        return new DirectCall<>(function);
    }

    public <T> Call<T> async(BiConsumer<ServerRequest, com.getfsc.retroserver.Callback<T>> function) {
        return new AsyncCall<>(function);
    }
}
