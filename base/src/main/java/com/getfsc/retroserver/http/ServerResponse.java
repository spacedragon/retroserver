package com.getfsc.retroserver.http;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 下午5:50
 */
public interface ServerResponse<T> {

    int code();

    ServerResponse<T> code(int code);

    ServerResponse<T> addHeader(String header, String value);

    ServerResponse<T> setHeader(String header, String value);

    String header(String header);

    boolean containsHeader(String header);

    T body();

    ServerResponse<T> setBody(T body);

    default ServerResponse<T> ok(T result) {
        return code(200)
                .setBody(result);
    }

    default ServerResponse<T> notFound(T result) {
        return error(404, result);
    }

    default ServerResponse<T> redirect(String url, T result) {
        return error(302, result).addHeader("location", url);
    }

    default ServerResponse<T> error(int code, T result) {
        return code(code)
                .setBody(result);
    }

}
