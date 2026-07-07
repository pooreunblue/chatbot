package com.example.archat.presentation.controller;

import com.example.archat.application.service.DefaultChatService;
import com.example.archat.domain.model.ChatAttachment;
import com.example.archat.infrastructure.repository.SupabaseChatRepository;
import com.example.archat.domain.service.ChatService;
import com.example.archat.presentation.dto.ChatResponseDTO;
import com.example.archat.presentation.dto.ConversationSummaryDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@WebServlet("/chat")
@MultipartConfig
public class ChatController extends BaseController {
    private ChatService chatService;
    private SupabaseChatRepository supabaseChatRepository;

    @Override
    public void init() throws ServletException {
        chatService = DefaultChatService.getInstance();
        supabaseChatRepository = SupabaseChatRepository.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String loginUserId = getLoginUserId(req);
        if (loginUserId == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        Long requestedConversationId = parseConversationId(req.getParameter("conversationId"));
        Long activeConversationId = requestedConversationId != null
                ? requestedConversationId
                : chatService.findLatestConversationId(loginUserId);

        List<ConversationSummaryDTO> conversations = chatService.findConversationsByUserId(loginUserId)
                .stream()
                .map(ConversationSummaryDTO::of)
                .toList();

        List<ChatResponseDTO> chats = chatService.findAllByConversationId(loginUserId, activeConversationId)
                .stream()
                .map(ChatResponseDTO::of)
                .toList();

        req.setAttribute("conversations", conversations);
        req.setAttribute("chats", chats);
        req.setAttribute("activeConversationId", activeConversationId);
        req.getRequestDispatcher("%s/%s".formatted(VIEW_PREFIX, "chat.jsp")).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String loginUserId = getLoginUserId(req);
        if (loginUserId == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        String action = req.getParameter("action");
        if (action != null && !action.isBlank()) {
            handleConversationAction(req, resp, loginUserId, action);
            return;
        }

        String message = req.getParameter("message");
        String model = req.getParameter("model");
        Long conversationId = parseConversationId(req.getParameter("conversationId"));

        if (message == null || message.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/chat");
            return;
        }

        if (conversationId == null) {
            conversationId = chatService.createConversation(loginUserId, message);
        }
        final Long activeConversationId = conversationId;

        List<ChatAttachment> attachments = req.getParts().stream()
                .filter(part -> "attachments".equals(part.getName()) && part.getSize() > 0)
                .map(part -> saveAttachment(loginUserId, activeConversationId, part))
                .toList();

        chatService.saveUserMessage(activeConversationId, loginUserId, message, model, attachments);
        resp.sendRedirect("%s/chat?conversationId=%d".formatted(req.getContextPath(), activeConversationId));
    }

    private void handleConversationAction(HttpServletRequest req, HttpServletResponse resp, String loginUserId, String action) throws IOException {
        Long conversationId = parseConversationId(req.getParameter("conversationId"));

        switch (action) {
            case "create" -> {
                Long newConversationId = chatService.createConversation(loginUserId, "New chat");
                resp.sendRedirect("%s/chat?conversationId=%d".formatted(req.getContextPath(), newConversationId));
            }
            case "rename" -> {
                if (conversationId != null) {
                    chatService.updateConversationTitle(loginUserId, conversationId, req.getParameter("title"));
                }
                resp.sendRedirect("%s/chat?conversationId=%d".formatted(req.getContextPath(), conversationId));
            }
            case "delete" -> {
                if (conversationId != null) {
                    chatService.deleteConversation(loginUserId, conversationId);
                }
                Long latestConversationId = chatService.findLatestConversationId(loginUserId);
                if (latestConversationId == null) {
                    resp.sendRedirect(req.getContextPath() + "/chat");
                } else {
                    resp.sendRedirect("%s/chat?conversationId=%d".formatted(req.getContextPath(), latestConversationId));
                }
            }
            default -> resp.sendRedirect(req.getContextPath() + "/chat");
        }
    }

    private ChatAttachment saveAttachment(String userId, Long conversationId, Part part) {
        String submittedFileName = part.getSubmittedFileName() == null ? "attachment" : part.getSubmittedFileName();
        String originalName = submittedFileName.contains("\\")
                ? submittedFileName.substring(submittedFileName.lastIndexOf('\\') + 1)
                : submittedFileName;

        try (InputStream inputStream = part.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            return supabaseChatRepository.storeAttachment(
                    userId,
                    conversationId,
                    originalName,
                    part.getContentType(),
                    outputStream.toByteArray()
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded attachment", e);
        }
    }

    private Long parseConversationId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getLoginUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("loginUserId");
    }
}
