package com.example.archat.presentation.dto;

import com.example.archat.domain.model.Chat;

public record ChatResponseDTO(
        Long conversationId,
        String owner,
        String model,
        String message,
        String timestamp
) {
    public Long getConversationId() {
        return conversationId;
    }

    public String getOwner() {
        return owner;
    }

    public String getModel() {
        return model;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public static ChatResponseDTO of(Chat chat) {
        return new ChatResponseDTO(
                chat.conversationId(),
                chat.owner(),
                chat.model(),
                chat.message(),
                chat.timestamp()
        );
    }
}
