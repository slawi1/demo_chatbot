package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class ChatBotConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String configName;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String jsonConfig;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
