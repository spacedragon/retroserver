package com.getfsc.retroserver.http;

import retrofit2.Call;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 下午6:26
 */
@FunctionalInterface
public interface RequestCaller {

    Call call(ServerRequest request);
}
