package com.example.archat.application.port;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;

import java.util.List;

public interface ChatProvider {
    String useAI(Chat chat);

    // ?�버 로딩
    String useAI(Chat newChat, List<Chat> chatHistory);

    String useAI(Chat newChat, List<Chat> chatHistory, List<ChatAttachment> attachments);
}
