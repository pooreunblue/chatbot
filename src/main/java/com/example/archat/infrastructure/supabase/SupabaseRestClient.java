package com.example.archat.infrastructure.supabase;

import com.example.archat.infrastructure.config.EnvironmentSettings;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class SupabaseRestClient {
    private static final String DEFAULT_SUPABASE_URL = "https://sbxlfgdwogpdondzyvia.supabase.co";
    private static final String DEFAULT_SUPABASE_KEY = "sb_publishable_XHbMwByaC6Niq019201IXA_CBg83ih0";
    private static final SupabaseRestClient instance = new SupabaseRestClient();

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String supabaseUrl = EnvironmentSettings.getOrDefault("SUPABASE_URL", DEFAULT_SUPABASE_URL);
    private final String supabaseKey = resolveServerKey();

    private SupabaseRestClient() {
    }

    public static SupabaseRestClient getInstance() {
        return instance;
    }

    private String resolveServerKey() {
        String serviceRoleKey = EnvironmentSettings.get("SUPABASE_SERVICE_ROLE_KEY");
        if (serviceRoleKey != null && !serviceRoleKey.isBlank()) {
            return serviceRoleKey;
        }
        return EnvironmentSettings.getOrDefault("SUPABASE_ANON_KEY", DEFAULT_SUPABASE_KEY);
    }

    public String get(String pathWithQuery) {
        return send("GET", pathWithQuery, null, false);
    }

    public String post(String pathWithQuery, String body, boolean returnRepresentation) {
        return send("POST", pathWithQuery, body, returnRepresentation);
    }

    public String patch(String pathWithQuery, String body, boolean returnRepresentation) {
        return send("PATCH", pathWithQuery, body, returnRepresentation);
    }

    public String delete(String pathWithQuery) {
        return send("DELETE", pathWithQuery, null, false);
    }

    public String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public String query(Map<String, String> params) {
        return params.entrySet()
                .stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String send(String method, String pathWithQuery, String body, boolean returnRepresentation) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/rest/v1/" + pathWithQuery))
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Content-Type", "application/json");

            if (returnRepresentation) {
                builder.header("Prefer", "return=representation");
            }

            if ("GET".equals(method)) {
                builder.GET();
            } else if ("DELETE".equals(method)) {
                builder.DELETE();
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Supabase REST request failed (%d): %s"
                        .formatted(response.statusCode(), response.body()));
            }
            return response.body();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call Supabase REST API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to call Supabase REST API", e);
        }
    }
}
