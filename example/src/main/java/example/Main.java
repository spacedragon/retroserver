package example;

import com.getfsc.retroserver.annotation.Bootstrap;
import com.getfsc.retroserver.server.ServerComponent;
import com.getfsc.retroserver.server.ServerOptions;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 上午11:09
 */


public class Main {

    @Bootstrap
    @Singleton
    @Component(modules = {ConfigMoudle.class, ControllerModule.class})
    interface App {
        ServerComponent server();
    }

    @Singleton
    @Module
    public static class ConfigMoudle {

        @Singleton
        @Provides
        ServerOptions serverOptions() {
            return new ServerOptions() {
                @Override
                public int getPort() {
                    return 8080;
                }
            };
        }
    }


    public static void main(String[] args) throws InterruptedException {
        App app = DaggerMain_App.builder()
                .configMoudle(new ConfigMoudle()).build();
        app.server().start();

    }
}
