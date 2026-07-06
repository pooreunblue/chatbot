package com.example.archat.application.service;

import com.example.archat.application.port.ChatProvider;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;
import com.example.archat.domain.model.ConversationSummary;
import com.example.archat.domain.repository.ChatRepository;
import com.example.archat.domain.service.ChatService;
import com.example.archat.infrastructure.api.GenAIChatProvider;
import com.example.archat.infrastructure.api.GroqChatProvider;
import com.example.archat.infrastructure.api.NIMChatProvider;
import com.example.archat.infrastructure.repository.SupabaseChatRepository;

import java.time.OffsetDateTime;
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

    private DefaultChatService() {
        this.chatRepository = SupabaseChatRepository.getInstance();
        this.geminiProvider = GenAIChatProvider.getInstance();
        this.groqProvider = GroqChatProvider.getInstance();
        this.nimProvider = NIMChatProvider.getInstance();
    }

    public static DefaultChatService getInstance() {
        return instance;
    }

    @Override
    public List<ConversationSummary> findConversationsByUserId(String userId) {
        return chatRepository.findConversationsByUserId(userId);
    }

    @Override
    public List<Chat> findAllByConversationId(String userId, Long conversationId) {
        if (conversationId == null) {
            return List.of();
        }
        return chatRepository.findAllByConversationId(userId, conversationId);
    }

    @Override
    public Long findLatestConversationId(String userId) {
        return chatRepository.findLatestConversationId(userId);
    }

    @Override
    public Long createConversation(String userId, String openingMessage) {
        return chatRepository.createConversation(userId, createConversationTitle(openingMessage));
    }

    @Override
    public void saveUserMessage(Long conversationId, String userId, String message, String model, List<ChatAttachment> attachments) {
        Chat userChat = new Chat(
                conversationId,
                message,
                "USER",
                userId,
                model,
                OffsetDateTime.now().toString()
        );
        chatRepository.save(userChat, attachments);

        List<Chat> history = chatRepository.findAllByConversationId(userId, conversationId);
        ChatProvider provider = selectProvider(model);
        String aiResponse = provider.useAI(userChat, history);

        Chat aiChat = new Chat(
                conversationId,
                aiResponse,
                "AI",
                userId,
                model,
                OffsetDateTime.now().toString()
        );
        chatRepository.save(aiChat, List.of());
        chatRepository.touchConversation(conversationId);
    }

    @Override
    public void updateConversationTitle(String userId, Long conversationId, String title) {
        chatRepository.updateConversationTitle(userId, conversationId, createConversationTitle(title));
    }

    @Override
    public void deleteConversation(String userId, Long conversationId) {
        chatRepository.deleteConversation(userId, conversationId);
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
        throw new IllegalArgumentException("Unsupported model: " + model);
    }

    private String createConversationTitle(String openingMessage) {
        String normalized = openingMessage == null ? "" : openingMessage.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "New chat";
        }
        return normalized.length() <= 48 ? normalized : normalized.substring(0, 48) + "...";
    }

    private static final DefaultChatService instance = new DefaultChatService();
}
