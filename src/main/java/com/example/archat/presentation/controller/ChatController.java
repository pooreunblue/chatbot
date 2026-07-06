package com.example.archat.presentation.controller;

import com.example.archat.application.service.DefaultChatService;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.service.ChatService;
import com.example.archat.presentation.dto.ChatResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@WebServlet("/chat")
public class ChatController extends BaseController {
    //    private GeminiChatService chatService;
    private ChatService chatService;
    // init

    @Override
    public void init() throws ServletException {
        chatService = DefaultChatService.getInstance(); // Lazy Loading
        // Service, Repository : static 저장해서 관리 <- tomcat이 자원 관리 X
        // Controller(Servlet) : tomcat 관리 - @WebServlet("/chat")
    }

    // get

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /// 로그인 진행하지 않는다면 무조건 index page로 돌아감
        String loginUserId = getLoginUserId(req);

        if (loginUserId == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        List<ChatResponseDTO> response = chatService.findAllByUserId(loginUserId)
                .stream()
                .map(ChatResponseDTO::of)
                .toList();

        // 세션 자체가 가지고 있는 id를 사용해서 인메모리 DB에서의 데이터를 구분
        req.setAttribute("chats",
                response);

        // 주소를 유지한채 jsp 포워딩 + 보안 + 가상 경로
        // webapp/WEB-INF/views/chat.jsp
        req.getRequestDispatcher("%s/%s".formatted(VIEW_PREFIX, "chat.jsp"))
                .forward(req, resp);
    }

    // post

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String loginUserId = getLoginUserId(req);

        if (loginUserId == null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        Chat chat = new Chat(
                req.getParameter("message"),
                "USER",
                loginUserId,
                req.getParameter("model"),
                ZonedDateTime.now().toString()
        );

        chatService.save(chat);
        resp.sendRedirect("%s/%s".formatted(req.getContextPath(), "chat"));
    }

    /// 사용자 ID 획득
    private String getLoginUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null) {
            return null;
        }

        return (String) session.getAttribute("loginUserId");
    }
}
