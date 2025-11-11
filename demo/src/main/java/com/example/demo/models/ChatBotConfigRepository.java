package com.example.demo.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatBotConfigRepository extends JpaRepository<ChatBotConfig, UUID> {
    Optional<ChatBotConfig> findByConfigName(String configName);
}
