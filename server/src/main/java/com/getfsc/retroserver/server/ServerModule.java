package com.getfsc.retroserver.server;

import com.getfsc.retroserver.Route;
import com.getfsc.retroserver.annotation.GlobalAop;
import com.getfsc.retroserver.aop.AopFactory;
import com.getfsc.retroserver.aop.AopFactoryHub;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 上午11:25
 */


@Module
public class ServerModule {

    @Provides
    @Singleton
    NettyServer server(ServerOptions serverOptions, Set<Route> routes, AopFactoryHub hub) {
        return new NettyServer(serverOptions, routes,hub);
    }

    @Provides(type = Provides.Type.SET_VALUES)
    Set<AopFactory> emptyFactories() {
        return Collections.emptySet();
    }

    @GlobalAop
    @Provides(type = Provides.Type.SET_VALUES)
    Set<AopFactory> emptyGlobalFactories() {
        return Collections.emptySet();
    }
}
