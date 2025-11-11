package com.example.demo.web;

import com.example.demo.models.ChatBotConfig;
import com.example.demo.models.ChatBotConfigRepository;
import com.example.demo.service.ChatFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/configuration")
public class ConfigurationController {
    private final ChatBotConfigRepository repository;
    private final ChatFlowService chatFlowService;

    public ConfigurationController(ChatBotConfigRepository repository, ChatFlowService chatFlowService) {
        this.repository = repository;
        this.chatFlowService = chatFlowService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadConfiguration(@RequestBody String jsonConfiguration, @RequestParam String name) {

        ChatBotConfig config = repository.findByConfigName(name)
                .orElse(new ChatBotConfig());
        config.setConfigName(name);
        config.setJsonConfig(jsonConfiguration);
        config.setUpdatedAt(LocalDateTime.now());
        if (config.getCreatedAt() == null) config.setCreatedAt(LocalDateTime.now());
        repository.save(config);
        return ResponseEntity.ok("Config saved successfully");
    }

    @PostMapping("/load/{name}")
    public ResponseEntity<String> loadConfig(@PathVariable String name) {
        chatFlowService.loadConfig(name);
        return ResponseEntity.ok("Config loaded successfully: " + name);
    }
}
