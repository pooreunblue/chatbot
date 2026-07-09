package com.example.archat.application.service;

import java.util.Map;

public final class ChatModelCatalog {

    public static final String IMAGE_ATTACHMENT_MODEL = "gemini-3.1-flash-lite";

    public enum ProviderType {
        GEMINI,
        GROQ,
        NIM
    }

    private static final Map<String, ProviderType> MODEL_PROVIDER_MAP = Map.ofEntries(
            Map.entry("gemini-3.1-flash-lite", ProviderType.GEMINI),
            Map.entry("gemma-4-26b-a4b-it", ProviderType.GEMINI),
            Map.entry("gemma-4-31b-it", ProviderType.GEMINI),

            Map.entry("openai/gpt-oss-20b", ProviderType.GROQ),
            Map.entry("openai/gpt-oss-120b", ProviderType.GROQ),
            Map.entry("llama-3.1-8b-instant", ProviderType.GROQ),
            Map.entry("llama-3.3-70b-versatile", ProviderType.GROQ),
            Map.entry("groq/compound", ProviderType.GROQ),
            Map.entry("groq/compound-mini", ProviderType.GROQ),
            Map.entry("qwen/qwen3-32b", ProviderType.GROQ),

            Map.entry("nvidia/nemotron-3-ultra-550b-a55b", ProviderType.NIM),
            Map.entry("deepseek-ai/deepseek-v4-pro", ProviderType.NIM)
    );

    private ChatModelCatalog() {
    }

    public static String requireSupportedModel(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("모델이 선택되지 않았습니다.");
        }

        if (!MODEL_PROVIDER_MAP.containsKey(model)) {
            throw new IllegalArgumentException("지원하지 않는 모델입니다: " + model);
        }

        return model;
    }

    public static ProviderType getProviderType(String model) {
        requireSupportedModel(model);
        return MODEL_PROVIDER_MAP.get(model);
    }

    public static String getImageAttachmentModel() {
        return IMAGE_ATTACHMENT_MODEL;
    }
}