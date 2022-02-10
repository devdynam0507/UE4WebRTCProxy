package com.codestory.ws.client;

import com.codestory.ws.WebRTCProxy;
import com.codestory.ws.server.Session;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
        logger.info("Try handshaking {}", handshaker.isHandshakeComplete());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        final Channel channel = ctx.channel();

        if(!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(channel, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            logger.info("Success handshaking {}", handshaker.isHandshakeComplete());
            return;
        }

        if(msg instanceof FullHttpResponse) {
            final FullHttpResponse res = (FullHttpResponse) msg;
            throw new Exception("Unexpected FullHttpResponse (getStatus=" + res.getStatus() + ", content="
                    + res.content().toString(CharsetUtil.UTF_8) + ')');
        }
        // 받은 데이터가 텍스트 일 경우
        else if(msg instanceof TextWebSocketFrame) {
            // Json 파싱을 진행함
            JSONObject jsonObject = new JSONObject(((TextWebSocketFrame) msg).text());
            // proxySessionId 값을 가져온다.
            String proxySessionId = jsonObject.getString(WebRTCProxy.keyOfProxySessionId);
            Optional<Session> session = Session.getSession(proxySessionId);

            // proxySessionId는 해당 프록시 서버를 거치면서 추가된 데이터이므로 다시 빼준다.
            jsonObject.remove(WebRTCProxy.keyOfProxySessionId);
            if(session.isPresent()) {
                // response 해준다.
                session.get().getChannel().writeAndFlush(new TextWebSocketFrame(jsonObject.toString()));
            }
        }
    }
}
