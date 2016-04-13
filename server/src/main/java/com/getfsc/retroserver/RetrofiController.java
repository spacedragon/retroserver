package com.getfsc.retroserver;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

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
}
