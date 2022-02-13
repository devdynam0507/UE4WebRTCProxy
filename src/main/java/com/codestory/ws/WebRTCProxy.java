package com.codestory.ws;

import com.codestory.ws.client.WebSocketClientInitializer;
import com.codestory.ws.server.HttpServerInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.net.URI;

public class WebRTCProxy {

    public static Logger logger = org.slf4j.LoggerFactory.getLogger(WebRTCProxy.class);
    public static Channel proxy;

    public static String keyOfProxySessionId = "proxySessionId";

    private static void startupWebSocketProxyServer() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024)
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new HttpServerInitializer());

        Channel channel = bootstrap.bind(5050).channel();
    }

    private static void startupWebSocketProxyClient(String host, int port) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        URI uri = URI.create(host);

        SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new WebSocketClientInitializer(host, sslCtx));

        proxy = bootstrap.connect(uri.getHost(), uri.getPort()).channel();
    }

    public static void main(String[] args) throws Exception {
        Thread serverThread = new Thread(() -> {
            try {
                startupWebSocketProxyClient("wss://45.32.249.81:8080/call", 8080);
                startupWebSocketProxyServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
    }

}
