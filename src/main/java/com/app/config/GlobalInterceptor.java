package com.app.config;

import com.app.session.UserSessionBean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class GlobalInterceptor implements HandlerInterceptor {

    private final UserSessionBean userSessionBean;

    public GlobalInterceptor(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        /**
         * CHECK IF WE NEED THE js AND CSS
         */
        // Allow access to login, oauth, static resources:
        if (path.startsWith("/login") || path.startsWith("/oauth2") || path.startsWith("/css") || path.startsWith("/js") || path.equals("/")) {
            // If already logged in → redirect /login to /home
            if ((path.startsWith("/login") || path.equals("/")) && userSessionBean.isLoggedIn()) {
                response.sendRedirect("/home");
                return false;
            }
            return true;
        }

        if (path.startsWith("/logout-confirm")) {
            if (!userSessionBean.isLoggedIn()) {
                response.sendRedirect("/login");
                return false;
            }
            return true;
        }

        // For all other pages → check if logged in
        if (!userSessionBean.isLoggedIn()) {
            response.sendRedirect("/login");
            return false;
        }

        return true; // Allow request to proceed
    }
}
