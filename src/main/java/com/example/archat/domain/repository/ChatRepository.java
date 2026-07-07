package com.example.archat.domain.repository;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;
import com.example.archat.domain.model.ConversationSummary;

import java.util.List;

public interface ChatRepository {
    Long createConversation(String userId, String title);

    void save(Chat chat, List<ChatAttachment> attachments);

    ChatAttachment storeAttachment(String userId, Long conversationId, String originalFileName, String mimeType, byte[] fileBytes);

    List<Chat> findAllByConversationId(String userId, Long conversationId);

    List<ConversationSummary> findConversationsByUserId(String userId);

    Long findLatestConversationId(String userId);

    void touchConversation(Long conversationId);

    void updateConversationTitle(String userId, Long conversationId, String title);

    void deleteConversation(String userId, Long conversationId);
}
