package com.example.archat.infrastructure.repository;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;
import com.example.archat.domain.model.ConversationSummary;
import com.example.archat.domain.repository.ChatRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryChatRepository implements ChatRepository {
    private static final InMemoryChatRepository instance = new InMemoryChatRepository();

    private final AtomicLong conversationSequence = new AtomicLong(1);
    private final Map<Long, ConversationSummary> conversations = new ConcurrentHashMap<>();
    private final Map<Long, List<Chat>> messagesByConversation = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> conversationsByUser = new ConcurrentHashMap<>();

    private InMemoryChatRepository() {
    }

    public static InMemoryChatRepository getInstance() {
        return instance;
    }

    @Override
    public Long createConversation(String userId, String title) {
        long conversationId = conversationSequence.getAndIncrement();
        ConversationSummary summary = new ConversationSummary(conversationId, title, OffsetDateTime.now().toString());
        conversations.put(conversationId, summary);
        conversationsByUser.computeIfAbsent(userId, key -> new ArrayList<>()).add(conversationId);
        return conversationId;
    }

    @Override
    public void save(Chat chat, List<ChatAttachment> attachments) {
        messagesByConversation.computeIfAbsent(chat.conversationId(), key -> new ArrayList<>()).add(chat);
        touchConversation(chat.conversationId());
    }

    @Override
    public List<Chat> findAllByConversationId(String userId, Long conversationId) {
        return messagesByConversation.getOrDefault(conversationId, List.of());
    }

    @Override
    public List<ConversationSummary> findConversationsByUserId(String userId) {
        return conversationsByUser.getOrDefault(userId, List.of())
                .stream()
                .map(conversations::get)
                .filter(summary -> summary != null)
                .sorted(Comparator.comparing(ConversationSummary::updatedAt).reversed())
                .toList();
    }

    @Override
    public Long findLatestConversationId(String userId) {
        return findConversationsByUserId(userId).stream()
                .findFirst()
                .map(ConversationSummary::conversationId)
                .orElse(null);
    }

    @Override
    public void touchConversation(Long conversationId) {
        ConversationSummary existing = conversations.get(conversationId);
        if (existing == null) {
            return;
        }
        conversations.put(
                conversationId,
                new ConversationSummary(existing.conversationId(), existing.title(), OffsetDateTime.now().toString())
        );
    }

    @Override
    public void updateConversationTitle(String userId, Long conversationId, String title) {
        ConversationSummary existing = conversations.get(conversationId);
        if (existing == null) {
            return;
        }
        conversations.put(
                conversationId,
                new ConversationSummary(existing.conversationId(), title, OffsetDateTime.now().toString())
        );
    }

    @Override
    public void deleteConversation(String userId, Long conversationId) {
        conversations.remove(conversationId);
        messagesByConversation.remove(conversationId);
        List<Long> userConversations = conversationsByUser.get(userId);
        if (userConversations != null) {
            userConversations.remove(conversationId);
        }
    }
}
