package org.entropy.producer.server;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Deprecated
//@Component
//@ServerEndpoint("/ws")
public class WsServer {
    // 当新的WebSocket连接建立时调用
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("New connection opened: " + session.getId());
    }

    // 当接收到WebSocket消息时调用
    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received message: " + message);
        try {
            // 回送消息
            session.getBasicRemote().sendText("Echo: " + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 当WebSocket连接关闭时调用
    @OnClose
    public void onClose(Session session) {
        System.out.println("Connection closed: " + session.getId());
    }

    // 当WebSocket发生错误时调用
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("Error on connection: " + session.getId());
        throw new RuntimeException(throwable);
    }
}
