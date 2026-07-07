package com.example.archat.presentation.dto;

import com.example.archat.domain.model.ConversationSummary;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record ConversationSummaryDTO(
        Long conversationId,
        String title,
        String updatedAt
) {
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KOREA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public Long getConversationId() {
        return conversationId;
    }

    public String getTitle() {
        return title;
    }

    public String getUpdatedAt() {
        return formatToKoreaTime(updatedAt);
    }

    public static ConversationSummaryDTO of(ConversationSummary conversationSummary) {
        return new ConversationSummaryDTO(
                conversationSummary.conversationId(),
                conversationSummary.title(),
                conversationSummary.updatedAt()
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
