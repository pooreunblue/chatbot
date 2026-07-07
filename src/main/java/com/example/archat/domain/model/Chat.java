package com.example.archat.domain.model;

public record Chat(
        Long conversationId,
        String message,
        String owner,
        String userId,
        String model,
        String timestamp
) {
}
