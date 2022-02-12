package com.codestory.ws.server;

import com.codestory.ws.WebRTCProxy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServerHandler extends ChannelInboundHandlerAdapter {

    private static Gson gson = new GsonBuilder().create();
    private static Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("is not null proxy : {}", WebRTCProxy.proxy != null);
        logger.info("proxy is opened : {}", WebRTCProxy.proxy.isOpen());
        if(WebRTCProxy.proxy != null) {
            Session.checkAndRegisterSession(ctx.channel());
            String sessionId = Session.getSession(ctx.channel()).get().getId();
            logger.info("received session id: {}", sessionId);

            // 언리얼에서 보낸 데이터가 Text 라면
            if(msg instanceof TextWebSocketFrame) {
                TextWebSocketFrame frame = (TextWebSocketFrame) msg;

                logger.info("text frame : {}", frame.text());
                // 들어온 데이터를 Json 파싱한다.
                JSONObject jsonObject = new JSONObject(frame.text());
                String sdp = jsonObject.getString("sdpOffer").replace("\\n", "\n");
                // 들어온 요청자의 proxy session을 json에 추가한다.
                jsonObject.put(WebRTCProxy.keyOfProxySessionId, sessionId);
                // UE에서 Json 파싱하는데에 개행있으면 \\n 으로 처리해줘야 하는데 이걸 다시 \n으로 바꿔줌
                jsonObject.put("sdpOffer", sdp);

                // Kurento 미디어 서버에 요청을 중계한다.
                WebRTCProxy.proxy.writeAndFlush(new TextWebSocketFrame(jsonObject.toString()));
            }
        }
    }
}
