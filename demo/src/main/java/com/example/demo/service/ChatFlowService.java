package com.example.demo.service;

import com.example.demo.models.ChatBotConfig;
import com.example.demo.models.ChatBotConfigRepository;
import com.example.demo.models.ChatResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ChatFlowService {

    private final ChatBotConfigRepository chatBotConfigRepository;
    private final ObjectMapper objectMapper;
    private Map<String, Object> currentConfig;


    public ChatFlowService(ChatBotConfigRepository chatBotConfigRepository, ObjectMapper objectMapper) {
        this.chatBotConfigRepository = chatBotConfigRepository;
        this.objectMapper = objectMapper;

    }

    public void loadConfig(String configName) {
        ChatBotConfig chatBotConfig = chatBotConfigRepository.findByConfigName(configName)
                .orElseThrow(() -> new RuntimeException("Config not found" + configName));

        try {
            this.currentConfig = objectMapper.readValue(chatBotConfig.getJsonConfig(), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Error parsing config " + configName, e);
        }
    }

    public List<Map<String, Object>> getAllFlow() {
        List<Map<String, Object>> flow = (List) currentConfig.get("start");
        return flow;

    }

    public Map<String, Object> getFirstBlock() {
        Map<String, Object> firstBlock = getAllFlow()
                .stream()
                .filter(block -> block.get("id").toString().equals("welcome"))
                .findFirst()
                .get();
        return firstBlock;
    }

    public Map<String, Object> getBlockById(String blockId) {
        Map<String, Object> currentBlock = getAllFlow()
                .stream()
                .filter(block -> block.get("id").toString().equals(blockId))
                .findFirst().orElseThrow(() -> new RuntimeException("Block not found"));
        return currentBlock;
    }

    public Map<String, Object> getNextBlock(String nextBlockId) {
        List<Map<String, Object>> flow = getAllFlow();
        return flow.stream()
                .filter(nextBlock -> nextBlock.get("id").equals(nextBlockId))
                .findFirst().orElseThrow(() -> new RuntimeException());
    }

    public ChatResponse handleUserMessage(String userMessage, String currentBlockId) {
        Map<String, Object> block = getBlockById(currentBlockId);

        ChatResponse chatResponse = new ChatResponse();

        switch (block.get("type").toString()) {
            case "writeMessage":
                chatResponse.setMessage(block.get("message").toString());
                chatResponse.setNextBlockId(block.get("next").toString());
                chatResponse.setCurrentBlockId(block.get("id").toString());
                break;

            case "waitForResponse":
                chatResponse.setNextBlockId(block.get("next").toString());
                chatResponse.setCurrentBlockId(block.get("id").toString());
                break;
            case "detectResponseIntent":
                List<String> options = (List<String>) block.get("options");
                if (options.contains(userMessage)) {
                    switch (userMessage) {
                        case "What is the weather":
                            chatResponse.setNextBlockId("weather");
                            break;
                        case "What is the time":
                            chatResponse.setNextBlockId("time");
                            break;
                        case "yes":
                            chatResponse.setNextBlockId("yes");
                            break;
                        case "no":
                            chatResponse.setNextBlockId("no");
                            break;
                        default:
                            chatResponse.setNextBlockId("notUnderstood");

                    }
                    chatResponse.setCurrentBlockId(block.get("id").toString());
                }
                break;
            default:
                break;
        }
        return chatResponse;
    }
}
