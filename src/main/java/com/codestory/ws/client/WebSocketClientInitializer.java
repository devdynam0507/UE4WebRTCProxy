package com.codestory.ws.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslContext;

import java.net.URI;

public class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {

    private final String host;
    private final SslContext sslContext;

    public WebSocketClientInitializer (String host, SslContext sslContext) {
        this.host = host;
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        URI uri = URI.create(host);

        if(sslContext != null) {
            pipeline.addLast(sslContext.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
        }

        pipeline.addLast("http-codec", new HttpClientCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
        pipeline.addLast("websocketClientHandler", new WebSocketClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(
                        uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders(), 1280000
                )
        ));
    }
}
