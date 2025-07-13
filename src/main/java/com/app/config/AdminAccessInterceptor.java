package com.app.config;

import com.app.model.User;
import com.app.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AdminAccessInterceptor implements HandlerInterceptor {
    private final CurrentUserService currentUserService;

    private static final Set<String> ALLOWED_PATHS_FOR_ADMIN = Set.of(
            "/admin",
            "/reports",
            "/home",
            "/logout",
            "/logout-confirm",
            "/error",
            "/files",
            "/api/broadcasts"
    );

    public AdminAccessInterceptor(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = currentUserService.getCurrentAppUser();

        if (user != null && "ADMIN".equals(user.getRole())) {
            String path = request.getRequestURI();
            boolean allowed = ALLOWED_PATHS_FOR_ADMIN.stream().anyMatch(path::startsWith);

            if (!allowed) {
                System.out.println("AdminAccessInterceptor: Blocking access to " + path);
                response.sendRedirect("/home");
                return false;
            }
        }

        return true;
    }
}
// begin cleanup