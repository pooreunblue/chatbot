package com.example.archat.presentation.controller;

import com.example.archat.domain.model.AuthUser;
import com.example.archat.infrastructure.auth.SupabaseAuthClient;
import com.example.archat.infrastructure.repository.SupabaseUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(urlPatterns = {
        "/auth/session",
        "/auth/logout"
})
public class AuthController extends BaseController {
    private final SupabaseAuthClient supabaseAuthClient = SupabaseAuthClient.getInstance();
    private final SupabaseUserRepository supabaseUserRepository = SupabaseUserRepository.getInstance();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getServletPath();

        if ("/auth/session".equals(path)) {
            handleSession(req, resp);
            return;
        }

        if ("/auth/logout".equals(path)) {
            handleLogout(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /// 로그인 관리
    private void handleSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String accessToken = req.getParameter("accessToken");

        if (accessToken == null || accessToken.isBlank()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            AuthUser user = supabaseAuthClient.getUser(accessToken);
            String appUserId = supabaseUserRepository.ensureUserId(user.email());

            HttpSession session = req.getSession();
            session.setAttribute("loginUserId", appUserId);
            session.setAttribute("loginEmail", user.email());
            session.setAttribute("loginAuthUserId", user.id());

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /// 로그아웃 관리
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        resp.sendRedirect(req.getContextPath() + "/"); // 메인 화면으로 이동
    }
}
