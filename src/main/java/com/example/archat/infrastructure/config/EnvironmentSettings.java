package com.example.archat.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class EnvironmentSettings {
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private EnvironmentSettings() {
    }

    public static String get(String key) {
        String value = System.getenv(key);
        return value != null && !value.isBlank() ? value : DOTENV.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static String require(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }
}
