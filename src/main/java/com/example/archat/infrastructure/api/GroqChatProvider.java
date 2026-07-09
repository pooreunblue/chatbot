package com.example.archat.infrastructure.api;

import com.example.archat.application.port.ChatProvider;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class GroqChatProvider implements ChatProvider {

    private static final URI DEFAULT_ENDPOINT =
            URI.create("https://api.groq.com/openai/v1/chat/completions");
    private static final Gson GSON = new Gson();
    private static final String SYSTEM_PROMPT = """
            친절한 말투로, 가능한 한글로 답변하세요.
            
            절대로 추론 과정, reasoning, chain of thought, thinking process를 출력하지 마세요.
            사용자의 질문에 대한 최종 답변만 출력하세요.
            """;

    private static final Message SYSTEM_MESSAGE =
            new Message("system", SYSTEM_PROMPT);

    private final HttpClient httpClient;
    private final URI endpoint;
    private final String apiKey;

    @Override
    public String useAI(Chat chat) {
        List<Message> messages = List.of(
                SYSTEM_MESSAGE,
                toMessage(chat)
        );
        return requestCompletion(chat.model(), messages);
    }

    @Override
    public String useAI(Chat newChat, List<Chat> chatHistory) {
        List<Message> messages = new java.util.ArrayList<>();
        messages.add(SYSTEM_MESSAGE);
        messages.addAll(
                chatHistory.stream()
                        .map(GroqChatProvider::toMessage)
                        .toList()
        );
        return requestCompletion(newChat.model(), messages);
    }

    @Override
    public String useAI(Chat newChat, List<Chat> chatHistory, List<ChatAttachment> attachments) {
        return useAI(newChat, chatHistory);
    }

    private GroqChatProvider() {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build(),
                DEFAULT_ENDPOINT,
                EnvironmentConfig.get("GROQ_API_KEY"));
    }

    GroqChatProvider(HttpClient httpClient, URI endpoint, String apiKey) {
        this.httpClient = httpClient;
        this.endpoint = endpoint;
        this.apiKey = apiKey;
    }

    private String requestCompletion(String model, List<Message> messages) {
        try {
            validateApiKey();
            ChatCompletionRequest body = new ChatCompletionRequest(model, messages);
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(extractError(response));
            }

            ChatCompletionResponse completion = GSON.fromJson(
                    response.body(),
                    ChatCompletionResponse.class);
            if (completion == null || completion.choices() == null
                    || completion.choices().isEmpty()
                    || completion.choices().get(0).message() == null
                    || completion.choices().get(0).message().content() == null) {
                throw new IllegalStateException("Groq API 응답에 메시지가 없습니다.");
            }
            return ProviderResponseUtil.cleanText(
                    completion.choices().get(0).message().content()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return errorMessage("Groq API 요청이 중단되었습니다.");
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return errorMessage(e.getMessage());
        }
    }

    private void validateApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GROQ_API_KEY 환경변수가 설정되지 않았습니다.");
        }
    }

    private static Message toMessage(Chat chat) {
        String role = "USER".equalsIgnoreCase(chat.owner()) ? "user" : "assistant";
        return new Message(role, chat.message());
    }

    private static String extractError(HttpResponse<String> response) {
        try {
            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject error = root.getAsJsonObject("error");
            if (error != null && error.has("message")) {
                return "Groq API 오류 (%d): %s"
                        .formatted(response.statusCode(), error.get("message").getAsString());
            }
        } catch (RuntimeException ignored) {
        }
        return "Groq API 오류 (HTTP %d)".formatted(response.statusCode());
    }

    private static String errorMessage(String message) {
        return ProviderResponseUtil.userFriendlyError("Groq");
    }

    private static final GroqChatProvider instance = new GroqChatProvider();

    public static GroqChatProvider getInstance() {
        return instance;
    }

    private record Message(String role, String content) {
    }

    private record ChatCompletionRequest(String model, List<Message> messages) {
    }

    private record ChatCompletionResponse(List<Choice> choices) {
    }

    private record Choice(Message message) {
    }
}
