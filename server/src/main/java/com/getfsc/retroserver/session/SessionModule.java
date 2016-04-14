package com.getfsc.retroserver.session;

import com.getfsc.retroserver.annotation.GlobalAop;
import com.getfsc.retroserver.aop.AopFactory;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.StringKey;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午9:28
 */

@Module
public class SessionModule {

    public static final String name = "SESSION";

    @GlobalAop
    @Provides(type= Provides.Type.SET)
    static AopFactory providesSessionFactory(SessionAopFactory factory){
        return factory;
    }
}
