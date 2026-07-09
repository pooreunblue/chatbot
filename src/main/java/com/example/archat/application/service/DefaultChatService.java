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

public class DefaultChatService implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatProvider geminiProvider;
    private final ChatProvider groqProvider;
    private final ChatProvider nimProvider;

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
        boolean hasImageAttachment = attachments != null && attachments.stream().anyMatch(ChatAttachment::isImage);
        String effectiveModel = hasImageAttachment
                ? ChatModelCatalog.getImageAttachmentModel()
                : ChatModelCatalog.requireSupportedModel(model);
        Chat userChat = new Chat(
                null,
                conversationId,
                message,
                "USER",
                userId,
                effectiveModel,
                OffsetDateTime.now().toString(),
                attachments
        );
        chatRepository.save(userChat, attachments);

        List<Chat> history = chatRepository.findAllByConversationId(userId, conversationId);
        ChatProvider provider = selectProvider(effectiveModel);
        String aiResponse = provider.useAI(userChat, history, attachments);

        Chat aiChat = new Chat(
                null,
                conversationId,
                aiResponse,
                "AI",
                userId,
                effectiveModel,
                OffsetDateTime.now().toString(),
                List.of()
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
        ChatModelCatalog.ProviderType providerType = ChatModelCatalog.getProviderType(model);

        return switch (providerType) {
            case GEMINI -> geminiProvider;
            case GROQ -> groqProvider;
            case NIM -> nimProvider;
        };
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
