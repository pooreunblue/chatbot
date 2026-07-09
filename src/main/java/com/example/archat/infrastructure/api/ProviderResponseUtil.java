package com.example.archat.infrastructure.api;

final class ProviderResponseUtil {

    private ProviderResponseUtil() {
    }

    static String cleanText(String text) {
        if (text == null || text.isBlank()) {
            return "응답을 생성하지 못했습니다. 다른 모델로 다시 시도해 주세요.";
        }

        String cleaned = text.trim();

        cleaned = removeThinkBlock(cleaned);
        cleaned = removeVisibleReasoningPrefix(cleaned);

        if (cleaned.isBlank()) {
            return "응답을 생성하지 못했습니다. 다른 모델로 다시 시도해 주세요.";
        }

        return cleaned;
    }

    static String userFriendlyError(String providerName) {
        return "%s 모델 응답을 생성하지 못했습니다. 잠시 후 다시 시도하거나 다른 모델을 선택해 주세요."
                .formatted(providerName);
    }

    static String userFriendlyTemporaryError(String providerName) {
        return "%s 모델이 일시적으로 혼잡합니다. 잠시 후 다시 시도하거나 다른 모델을 선택해 주세요."
                .formatted(providerName);
    }

    private static String removeThinkBlock(String text) {
        return text.replaceAll("(?is)<think>.*?</think>", "").trim();
    }

    private static String removeVisibleReasoningPrefix(String text) {
        String lower = text.toLowerCase();

        if (lower.startsWith("okay, the user")
                || lower.startsWith("we need")
                || lower.startsWith("the user")
                || lower.startsWith("i need to")) {

            int koreanStart = findFirstKoreanIndex(text);

            if (koreanStart > 0) {
                return text.substring(koreanStart).trim();
            }
        }

        return text;
    }

    private static int findFirstKoreanIndex(String text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch >= '가' && ch <= '힣') {
                return i;
            }
        }

        return -1;
    }
}