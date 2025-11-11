package com.example.demo.config;

import com.example.demo.models.ChatResponse;
import com.example.demo.service.ChatFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final ChatFlowService flowService;


    public WebSocketHandler(ChatFlowService flowService) {
        this.flowService = flowService;

    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> firstBlock = flowService.getFirstBlock();

        if (firstBlock != null && firstBlock.get("type").equals("writeMessage")) {
            session.sendMessage(new TextMessage(firstBlock.get("message").toString()));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        Map<String, String> history = new LinkedHashMap<>();

        Map<String, Object> currentBlock = flowService.getNextBlock(flowService.getFirstBlock().get("next").toString());
        String userMessage;

        while (currentBlock.get("next").toString() != null) {
            if (currentBlock.get("type").equals("waitForResponse")) {
                userMessage = message.getPayload();
                currentBlock = flowService.getNextBlock(currentBlock.get("next").toString());
                continue;
            }
            userMessage = message.getPayload();
            ChatResponse chat = flowService.handleUserMessage(userMessage, currentBlock.get("id").toString());

            if (currentBlock.get("type").equals("writeMessage")) {
                session.sendMessage(new TextMessage(chat.getMessage()));
            }
            chat = flowService.handleUserMessage(userMessage, chat.getNextBlockId());
            currentBlock = flowService.getNextBlock(chat.getCurrentBlockId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }
}
