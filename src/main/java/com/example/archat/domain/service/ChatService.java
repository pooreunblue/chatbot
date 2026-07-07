package com.example.archat.domain.service;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;
import com.example.archat.domain.model.ConversationSummary;

import java.util.List;

public interface ChatService {
    List<ConversationSummary> findConversationsByUserId(String userId);

    List<Chat> findAllByConversationId(String userId, Long conversationId);

    Long findLatestConversationId(String userId);

    Long createConversation(String userId, String openingMessage);

    void saveUserMessage(Long conversationId, String userId, String message, String model, List<ChatAttachment> attachments);

    void updateConversationTitle(String userId, Long conversationId, String title);

    void deleteConversation(String userId, Long conversationId);
}
