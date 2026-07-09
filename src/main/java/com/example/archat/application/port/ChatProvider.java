package com.example.archat.application.port;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.model.ChatAttachment;

import java.util.List;

public interface ChatProvider {
    String useAI(Chat chat);

    // 오버 로딩
    String useAI(Chat newChat, List<Chat> chatHistory);

    // 첨부파일을 처리하지 않는 Provider는 기본적으로 히스토리 기반 호출을 사용
    default String useAI(Chat newChat, List<Chat> chatHistory, List<ChatAttachment> attachments) {
        return useAI(newChat, chatHistory);
    }
}
