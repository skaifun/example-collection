package org.examples.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EchoWebSocketHandlerTest {

    @LocalServerPort
    private int port;

    @Test
    public void testWebSocketCommunication() throws Exception {
        // 1. 创建客户端和消息队列
        StandardWebSocketClient client = new StandardWebSocketClient();
        BlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();

        // 2. 建立连接
        String websocketEndpoint = "ws://localhost:%d/ws/echo".formatted(port);
        WebSocketSession session = client.execute(
            new TextWebSocketHandler() {
                @Override
                protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
                    receivedMessages.add(message.getPayload()); // 接收消息
                }
            },
            websocketEndpoint // WebSocket 地址
        ).get(5, TimeUnit.SECONDS); // 设置超时

        // 3. 发送测试消息
        String testMessage = "hello, world!";
        session.sendMessage(new TextMessage(testMessage));

        // 4. 异步验证响应
        String expectedMessage = "Echo: hello, world!";
        await().atMost(5, TimeUnit.SECONDS).until(() -> !receivedMessages.isEmpty());
        String response = receivedMessages.poll();
        assertEquals(expectedMessage, response);

        session.close(); // 关闭连接
    }
}
