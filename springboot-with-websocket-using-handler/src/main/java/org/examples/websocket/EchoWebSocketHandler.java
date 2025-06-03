package org.examples.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class EchoWebSocketHandler extends TextWebSocketHandler {
    public static final Logger LOGGER = LoggerFactory.getLogger(EchoWebSocketHandler.class);

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        LOGGER.info("message received: {}({} bytes)", message.getPayload().stripIndent(), message.getPayloadLength());
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }
}
