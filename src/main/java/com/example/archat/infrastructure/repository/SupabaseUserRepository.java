package com.example.archat.infrastructure.repository;

import com.example.archat.infrastructure.supabase.SupabaseRestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SupabaseUserRepository {
    private static final SupabaseUserRepository instance = new SupabaseUserRepository();

    private final SupabaseRestClient supabaseRestClient = SupabaseRestClient.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private SupabaseUserRepository() {
    }

    public static SupabaseUserRepository getInstance() {
        return instance;
    }

    public String ensureUserId(String email) {
        try {
            String existingResponse = supabaseRestClient.get(
                    "users?email=eq." + supabaseRestClient.encode(email)
                            + "&select=user_id&limit=1"
            );
            JsonNode existingArray = objectMapper.readTree(existingResponse);
            if (existingArray.isArray() && !existingArray.isEmpty()) {
                String userId = existingArray.get(0).path("user_id").asText();
                updateLastLoginAt(userId);
                return userId;
            }

            String now = OffsetDateTime.now().toString();
            String generatedPasswordHash = "supabase-oauth:" + UUID.nameUUIDFromBytes(email.getBytes(StandardCharsets.UTF_8));
            String insertResponse = supabaseRestClient.post(
                    "users?select=user_id",
                    objectMapper.writeValueAsString(new UserInsertPayload(email, generatedPasswordHash, now)),
                    true
            );
            JsonNode insertedArray = objectMapper.readTree(insertResponse);
            if (!insertedArray.isArray() || insertedArray.isEmpty()) {
                throw new IllegalStateException("User insert did not return an id");
            }
            return insertedArray.get(0).path("user_id").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sync user with Supabase database: " + e.getMessage(), e);
        }
    }

    private void updateLastLoginAt(String userId) throws Exception {
        supabaseRestClient.patch(
                "users?user_id=eq." + userId,
                objectMapper.writeValueAsString(new UserLoginPayload(OffsetDateTime.now().toString())),
                false
        );
    }

    private record UserInsertPayload(String email, String password_hash, String last_login_at) {}
    private record UserLoginPayload(String last_login_at) {}
}
