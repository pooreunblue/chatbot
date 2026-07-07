package com.example.archat.application.service;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;
import com.example.archat.domain.model.ConversationSummary;
import com.example.archat.domain.service.ChatService;

import java.util.List;

public class AIChatService implements ChatService {
    private final ChatService delegate = DefaultChatService.getInstance();

    @Override
    public List<ConversationSummary> findConversationsByUserId(String userId) {
        return delegate.findConversationsByUserId(userId);
    }

    @Override
    public List<Chat> findAllByConversationId(String userId, Long conversationId) {
        return delegate.findAllByConversationId(userId, conversationId);
    }

    @Override
    public Long findLatestConversationId(String userId) {
        return delegate.findLatestConversationId(userId);
    }

    @Override
    public Long createConversation(String userId, String openingMessage) {
        return delegate.createConversation(userId, openingMessage);
    }

    @Override
    public void saveUserMessage(Long conversationId, String userId, String message, String model, List<ChatAttachment> attachments) {
        delegate.saveUserMessage(conversationId, userId, message, model, attachments);
    }

    @Override
    public void updateConversationTitle(String userId, Long conversationId, String title) {
        delegate.updateConversationTitle(userId, conversationId, title);
    }

    @Override
    public void deleteConversation(String userId, Long conversationId) {
        delegate.deleteConversation(userId, conversationId);
    }
}
