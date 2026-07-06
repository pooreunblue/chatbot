package com.example.archat.infrastructure.api;

import io.github.cdimascio.dotenv.Dotenv;

final class EnvironmentConfig {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private EnvironmentConfig() {
    }

    static String get(String name) {
        String environmentValue = System.getenv(name);
        return environmentValue != null ? environmentValue : DOTENV.get(name);
    }
}
