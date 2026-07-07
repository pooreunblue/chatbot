package com.example.archat.domain.model;

import java.util.List;

public record Chat(
        Long messageId,
        Long conversationId,
        String message,
        String owner,
        String userId,
        String model,
        String timestamp,
        List<ChatAttachment> attachments
) {
}
