package com.example.archat.presentation.dto;

import com.example.archat.domain.model.ConversationSummary;

public record ConversationSummaryDTO(
        Long conversationId,
        String title,
        String updatedAt
) {
    public Long getConversationId() {
        return conversationId;
    }

    public String getTitle() {
        return title;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public static ConversationSummaryDTO of(ConversationSummary conversationSummary) {
        return new ConversationSummaryDTO(
                conversationSummary.conversationId(),
                conversationSummary.title(),
                conversationSummary.updatedAt()
        );
    }
}
