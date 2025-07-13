package com.app.config;

import com.app.model.User;
import com.app.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Intercepts all incoming HTTP requests to enforce access restrictions for ADMIN users.
 *
 * This interceptor ensures that ADMIN users can only access a limited set of allowed paths
 * (e.g., admin panel, reports, logout, etc.), and are redirected to "/home" if they attempt
 * to access any unauthorized page.
 */
@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * A set of URI path prefixes that are allowed for ADMIN users.
     */
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

    /**
     * Checks each incoming request to determine if an ADMIN user is accessing a permitted path.
     * If the ADMIN is accessing a disallowed path, they are redirected to "/home".
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param handler  the chosen handler to execute
     * @return true if the request should proceed, false if redirected
     * @throws Exception in case of processing error
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = currentUserService.getCurrentAppUser();

        if (user != null && "ADMIN".equals(user.getRole())) {
            String path = request.getRequestURI();
            boolean allowed = ALLOWED_PATHS_FOR_ADMIN.stream().anyMatch(path::startsWith);

            if (!allowed) {
                response.sendRedirect("/home");
                return false;
            }
        }

        return true;
    }
}