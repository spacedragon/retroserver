package com.getfsc.retroserver.auth;

import com.getfsc.retroserver.annotation.GlobalAop;
import com.getfsc.retroserver.aop.AopFactory;
import com.getfsc.retroserver.session.SessionModule;
import dagger.Module;
import dagger.Provides;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/14
 * Time: 上午10:49
 */

@Module(includes = SessionModule.class)
public class AuthModule {

    @GlobalAop
    @Provides(type = Provides.Type.SET)
    AopFactory authAopFactory() {
        return () -> request -> {
            request.setObject(LoginProvider.class, new LoginProvider(request));
            return true;
        };
    }


    @Provides(type = Provides.Type.SET)
    AopFactory checkAopFactory() {
        return new CheckAopFactory();
    }

}
