package org.entropy.producer.handler;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class MyWebSocketHandler extends TextWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        // 处理接收到的消息
        System.out.println("Received: " + payload);

        // 回复消息
        session.sendMessage(new TextMessage("Echo: " + payload));
    }
}
