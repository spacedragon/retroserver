package com.getfsc.retroserver.util;

import java.util.function.Supplier;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 下午8:54
 */
public class H {
    public static RuntimeException rte(Throwable e) {
        return new RuntimeException(e);
    }

    public static RuntimeException rte(String s) {
        return new RuntimeException(s);
    }

    public static <T> T ifNull(T t, Supplier<T> supplier) {
         return t == null ? supplier.get() : t;
    }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
