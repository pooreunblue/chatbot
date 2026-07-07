package com.example.archat.presentation.dto;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;

import java.util.List;

public record ChatResponseDTO(
        Long messageId,
        Long conversationId,
        String owner,
        String model,
        String message,
        String timestamp,
        List<ChatAttachment> attachments
) {
    public Long getMessageId() {
        return messageId;
    }

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

    public List<ChatAttachment> getAttachments() {
        return attachments;
    }

    public static ChatResponseDTO of(Chat chat) {
        return new ChatResponseDTO(
                chat.messageId(),
                chat.conversationId(),
                chat.owner(),
                chat.model(),
                chat.message(),
                chat.timestamp(),
                chat.attachments()
        );
    }
}
