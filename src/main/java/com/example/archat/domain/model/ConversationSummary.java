package com.example.archat.domain.model;

public record ConversationSummary(
        Long conversationId,
        String title,
        String updatedAt
) {
}
