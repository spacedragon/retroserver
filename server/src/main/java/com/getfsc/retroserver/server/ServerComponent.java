package com.getfsc.retroserver.server;

import dagger.Subcomponent;

import javax.inject.Singleton;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/12
 * Time: 上午11:28
 */
@Singleton
@Subcomponent(modules = {ServerModule.class})
public interface ServerComponent {

    NettyServer server();

    default void start(){
        NettyServer server = server();
        server.start();
        try {
            server.waitForShutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
