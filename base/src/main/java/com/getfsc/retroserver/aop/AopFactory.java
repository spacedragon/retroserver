package com.getfsc.retroserver.aop;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午6:48
 */
public interface AopFactory {
    default String name(){
        return this.getClass().getName();
    }

    AopInterceptor create();

    default AopFactory setFactoryParams(Object[] params){
        return this;
    };

    default Integer priority() {
        return 9999;
    };

}
