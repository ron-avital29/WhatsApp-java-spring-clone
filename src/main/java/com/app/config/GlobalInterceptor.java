package com.app.config;

import com.app.model.User;
import com.app.repo.UserRepository;
import com.app.session.UserSessionBean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts all requests to enforce login status and ban logic for users.
 */
@Component
public class GlobalInterceptor implements HandlerInterceptor {

    @Autowired
    private UserSessionBean userSessionBean;

    @Autowired
    private UserRepository userRepository;

    /**
     * This method is called before the actual handler is executed.
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (isPublicPath(path)) {
            if ((path.equals("/") || path.startsWith("/login")) && userSessionBean.isLoggedIn()) {
                response.sendRedirect("/home");
                return false;
            }
            return true;
        }

        if (!userSessionBean.isLoggedIn()) {
            response.sendRedirect("/login");
            return false;
        }

        if (!path.startsWith("/banned")) {
            User sessionUser = userSessionBean.getUser();
            if (sessionUser != null) {
                User latestUser = userRepository.findById(sessionUser.getId()).orElseThrow();
                if (latestUser.isBanned()) {
                    request.getSession().setAttribute("bannedUntil", latestUser.getBannedUntil());
                    response.sendRedirect("/banned");
                    return false;
                }
            }
        }


        return true;
    }

    /**
     * Determines if the requested path is public and does not require authentication.
     * @param path The request URI.
     * @return true if the path is public, false otherwise.
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/login") ||
        path.startsWith("/oauth2") ||
        path.startsWith("/css") ||
        path.startsWith("/js") ||
        path.startsWith("/banned") ||
                path.equals("/");
    }
}
