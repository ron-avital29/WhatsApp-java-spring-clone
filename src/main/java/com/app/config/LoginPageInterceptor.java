package com.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginPageInterceptor implements HandlerInterceptor {


    /**
     * NOTE - WE NEVER GWT HERE. IT'S A BUG. MUST CHECK WHY.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("LoginPageInterceptor preHandle");
        String requestUri = request.getRequestURI();

        // If user is authenticated and accessing "/" or "/login", redirect to /home
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof OAuth2User
                && requestUri.equals("/login")) {

            System.out.println("User is authenticated, redirecting to /home");
            response.sendRedirect("/home");
            return false; // Stop processing this request
        }

        return true; // Continue processing
    }
}
