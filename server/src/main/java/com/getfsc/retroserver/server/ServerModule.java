package com.getfsc.retroserver.server;

import com.getfsc.retroserver.Route;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
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
    NettyServer server(ServerOptions serverOptions,Set<Route> routes) {
        return new NettyServer(serverOptions,routes);
    }
}
