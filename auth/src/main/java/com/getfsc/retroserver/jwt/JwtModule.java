package com.getfsc.retroserver.jwt;

import com.getfsc.retroserver.aop.AopFactory;
import dagger.Module;
import dagger.Provides;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/13
 * Time: 下午8:08
 */
@Module
public class JwtModule {


    @Provides(type= Provides.Type.SET)
    static AopFactory provideJwtFactory(JwtAopFactory factory){
        return factory;
    }


}
