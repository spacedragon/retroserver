package com.getfsc.retroserver;

import com.getfsc.retroserver.request.ServerRequest;
import okhttp3.Protocol;
import retrofit2.Call;
import retrofit2.Response;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/10
 * Time: 下午12:01
 */
public class RetrofiController {

    protected <T> Call<T> ok(T result) {
        return new DirectCall<>(request -> Response.success(result));
    }

    protected <T> Call<T> errorCode(int code, T result) {
        return new DirectCall<>(request -> {
            okhttp3.Response raw = new okhttp3.Response.Builder()
                    .code(code)
                    .protocol(Protocol.HTTP_1_1)
                    .build();
            return Response.success(result, raw);
        });
    }

    protected <T> Call<T> handle(BiFunction<ServerRequest, okhttp3.Response.Builder, T> handler) {
        return new DirectCall<>(request -> {
            okhttp3.Response.Builder builder = new okhttp3.Response.Builder();
            T result = handler.apply(request, builder);
            return Response.success(result, builder.build());
        });
    }
}
