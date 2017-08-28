package com.getfsc.retroserver.server;

import com.getfsc.retroserver.Route;
import com.getfsc.retroserver.aop.AopFactoryHub;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.router.Router;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import javax.inject.Inject;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/11
 * Time: 下午2:37
 */
public class NettyServer {

    private ServerOptions options;
    private static final InternalLogger log = InternalLoggerFactory.getInstance(NettyServer.class);

    @Inject
    public NettyServer(ServerOptions options, Set<Route> routes, AopFactoryHub hub) {
        this.options = options;
        this.routes = routes;
        this.hub = hub;
    }

    private Set<Route> routes;
    private AopFactoryHub hub;

    public void start() {
        try {
            Router<Route> router = new Router<>();

            installRoutes(router);

            final SslContext sslCtx;
            if (options.ssl()) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            } else {
                sslCtx = null;
            }

            // Configure the server.
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.option(ChannelOption.SO_REUSEADDR, true);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer(sslCtx, router));
            b.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true));
            b.childOption(ChannelOption.SO_REUSEADDR, true);

            ch = b.bind(options.getPort()).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                    (options.ssl() ? "https" : "http") + "://127.0.0.1:" + options.getPort() + '/');
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void installRoutes(Router<Route> router) {
        for (Route route : routes) {
            String path = join(route).replaceAll("\\{(.*?)\\}", ":$1");
            log.debug("mounting route path: {} {}", route.getVerb(), path);
            route.installAops(hub);
            router.addRoute(HttpMethod.valueOf(route.getVerb()), path, route);
        }
        router.notFound(Route.NotFound);
    }

    private String join(Route route) {
        String base = route.getBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String url = route.getUrl();
        if (url.startsWith("/")) {
            url = url.substring(1);
        }
        return base + "/" + url;
    }


    private Channel ch;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;


    public void waitForShutdown() throws InterruptedException {
        try {
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void waitForShutdown1() throws InterruptedException {
        try {
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
