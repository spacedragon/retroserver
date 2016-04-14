package example;

import com.getfsc.retroserver.annotation.Bootstrap;
import com.getfsc.retroserver.auth.AuthModule;
import com.getfsc.retroserver.jwt.JwtModule;
import com.getfsc.retroserver.jwt.JwtOptions;
import com.getfsc.retroserver.server.ServerComponent;
import com.getfsc.retroserver.server.ServerModule;
import com.getfsc.retroserver.server.ServerOptions;
import com.getfsc.retroserver.session.MemorySessionProvider;
import com.getfsc.retroserver.session.SessionModule;
import com.getfsc.retroserver.session.SessionProvider;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.time.Duration;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 上午11:09
 */


public class Main {

    @Bootstrap
    @Singleton
    @Component(modules = {ConfigMoudle.class, ControllerModule.class,AuthModule.class, JwtModule.class})
    interface App {
        ServerComponent server();
    }

    @Singleton
    @Module
    public static class ConfigMoudle {

        @Provides
        @Singleton
        SessionProvider sessionProvider(){
            return new MemorySessionProvider();
        }

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

        @Singleton
        @Provides
        JwtOptions jwtOptions() {
            return new JwtOptions() {
                @Override
                public byte[] jwtSecret() {
                    return new byte[]{111, 123, 121};
                }

                @Override
                public Duration jwtExpiration() {
                    return Duration.ZERO;
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
