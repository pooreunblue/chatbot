package com.example.archat.application.service;

import com.example.archat.application.port.ChatProvider;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.repository.ChatRepository;
import com.example.archat.domain.service.ChatService;
import com.example.archat.infrastructure.api.GenAIChatProvider;
import com.example.archat.infrastructure.api.GroqChatProvider;
import com.example.archat.infrastructure.api.NIMChatProvider;
import com.example.archat.infrastructure.repository.InMemoryChatRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public class DefaultChatService implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatProvider geminiProvider;
    private final ChatProvider groqProvider;
    private final ChatProvider nimProvider;

    private static final Set<String> GEMINI_MODELS = Set.of(
            "gemini-3.1-flash-lite",
            "gemma-4-26b-a4b-it",
            "gemma-4-31b-it"
    );

    private static final Set<String> GROQ_MODELS = Set.of(
            "openai/gpt-oss-20b",
            "openai/gpt-oss-120b",
            "llama-3.1-8b-instant",
            "llama-3.3-70b-versatile",
            "groq/compound",
            "groq/compound-mini",
            "qwen/qwen3-32b"
    );

    private static final Set<String> NIM_MODELS = Set.of(
            "nvidia/nemotron-3-ultra-550b-a55b",
            "deepseek-ai/deepseek-v4-pro"
    );

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

    private DefaultChatService() {
        this.chatRepository = InMemoryChatRepository.getInstance();
        this.geminiProvider = GenAIChatProvider.getInstance();
        this.groqProvider = GroqChatProvider.getInstance();
        this.nimProvider = NIMChatProvider.getInstance();
    }

    private ChatProvider selectProvider(String model) {

        if (GEMINI_MODELS.contains(model)) {
            return geminiProvider;
        }

        if (GROQ_MODELS.contains(model)) {
            return groqProvider;
        }

        if (NIM_MODELS.contains(model)) {
            return nimProvider;
        }

        throw new IllegalArgumentException(
                "지원하지 않는 모델입니다 : " + model);
    }

    private static final DefaultChatService instance = new DefaultChatService();

    public static DefaultChatService getInstance() {
        return instance;
    }

}
