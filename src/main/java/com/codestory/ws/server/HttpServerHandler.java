package com.codestory.ws.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private WebSocketServerHandshaker webSocketServerHandshaker;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest)msg;
            HttpHeaders httpHeaders = req.headers();

            if(httpHeaders.get(HttpHeaderNames.CONNECTION).equals("Upgrade") &&
                    httpHeaders.get(HttpHeaderNames.UPGRADE).equalsIgnoreCase("WebSocket")) {
                ctx.pipeline().replace(this, "websocketHandler", new WebSocketServerHandler());

                handleHandshake(ctx, req);
            }
        }
    }

    protected void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) {
        WebSocketServerHandshakerFactory wsHandshakerFactory = new WebSocketServerHandshakerFactory("ws://" + req.headers().get("Host") + req.uri(), null, true);
        webSocketServerHandshaker = wsHandshakerFactory.newHandshaker(req);

        if(webSocketServerHandshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        else {
            webSocketServerHandshaker.handshake(ctx.channel(), req);
        }
    }
}
