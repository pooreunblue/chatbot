package com.example.archat.infrastructure.api;

import com.example.archat.application.port.ChatProvider;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.FileData;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.util.ArrayList;
import java.util.List;

public class GenAIChatProvider implements ChatProvider {

    @Override
    public String useAI(Chat chat) {
        try (Client client = GenAIConfig.getClient()) {
            GenerateContentResponse response = client.models.generateContent(
                    chat.model(),
                    chat.message(),
                    GenAIConfig.getGenerateContentConfig());
            return ProviderResponseUtil.cleanText(response.text());
        } catch (Exception e) {
            e.printStackTrace();
            return ProviderResponseUtil.userFriendlyTemporaryError("Gemini");
        }
    }

    @Override
    public String useAI(Chat newChat, List<Chat> chatHistory) {
        return useAI(newChat, chatHistory, List.of());
    }

    @Override
    public String useAI(Chat newChat, List<Chat> chatHistory, List<ChatAttachment> attachments) {
        List<Content> contents = new ArrayList<>();
        for (Chat chat : chatHistory) {
            contents.add(Content.builder()
                    .role("USER".equalsIgnoreCase(chat.owner()) ? "user" : "model")
                    .parts(Part.builder().text(chat.message()).build())
                    .build());
        }

        List<Part> currentParts = new ArrayList<>();
        currentParts.add(Part.builder().text(newChat.message()).build());
        for (ChatAttachment attachment : attachments == null ? List.<ChatAttachment>of() : attachments) {
            if (attachment.isImage() && attachment.getFilePath() != null && !attachment.getFilePath().isBlank()) {
                currentParts.add(
                        Part.builder()
                                .fileData(FileData.builder()
                                        .mimeType(attachment.getMimeType())
                                        .fileUri(attachment.getFilePath())
                                        .build())
                                .build()
                );
            }
        }
        contents.add(Content.builder()
                .role("user")
                .parts(currentParts)
                .build());

        try (Client client = GenAIConfig.getClient()) {
            GenerateContentResponse response = client.models.generateContent(
                    newChat.model(),
                    contents,
                    GenAIConfig.getGenerateContentConfig());
            return ProviderResponseUtil.cleanText(response.text());
        } catch (Exception e) {
            e.printStackTrace();
            return ProviderResponseUtil.userFriendlyTemporaryError("Gemini");
        }
    }

    private GenAIChatProvider() {

    }

    private static final GenAIChatProvider instance = new GenAIChatProvider();

    public static GenAIChatProvider getInstance() {
        return instance;
    }

}