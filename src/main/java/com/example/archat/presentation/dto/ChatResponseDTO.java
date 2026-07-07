package com.example.archat.presentation.dto;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KOREA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        return formatToKoreaTime(timestamp);
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

    private static String formatToKoreaTime(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return OffsetDateTime.parse(value)
                    .atZoneSameInstant(KOREA_ZONE)
                    .format(KOREA_FORMATTER);
        } catch (Exception e) {
            return value;
        }
    }
}
