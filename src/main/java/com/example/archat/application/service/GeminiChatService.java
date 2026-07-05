package com.example.archat.application.service;

import com.example.archat.application.port.ChatProvider;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.repository.ChatRepository;
import com.example.archat.domain.service.ChatService;
import com.example.archat.infrastructure.api.GenAIChatProvider;
import com.example.archat.infrastructure.api.GroqChatProvider;
import com.example.archat.infrastructure.repository.InMemoryChatRepository;

import java.time.ZonedDateTime;
import java.util.List;

public class GeminiChatService implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatProvider geminiProvider;
    private final ChatProvider groqProvider;

    @Override
    public void save(Chat chat) {
        chatRepository.save(chat);
//        String aiResponse = useAI(chat);
        List<Chat> history = chatRepository.findAllByUserId(chat.userId());
        ChatProvider provider = selectProvider(chat.model());
        String aiResponse = provider.useAI(chat, history);
        Chat aiChat = new Chat(
                aiResponse,
                "AI",
                chat.userId(),
                chat.model(),
                ZonedDateTime.now().toString()
        );
        chatRepository.save(aiChat);
    }

    @Override
    public List<Chat> findAllByUserId(String userId) {
        return chatRepository.findAllByUserId(userId);
    }

    // 싱글톤 등록

    private GeminiChatService() {
        this.chatRepository = InMemoryChatRepository.getInstance();
        this.geminiProvider = GenAIChatProvider.getInstance();
        this.groqProvider = GroqChatProvider.getInstance();
    }

    private ChatProvider selectProvider(String model) {

        model = model.toLowerCase();

        if (model.startsWith("gemini")
                || model.startsWith("gemma")) {
            return geminiProvider;
        }

        return groqProvider;
    }
    private static final GeminiChatService instance = new GeminiChatService();

    public static GeminiChatService getInstance() {
        return instance;
    }

}
