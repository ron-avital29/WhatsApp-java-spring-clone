package com.app.config;

import com.app.model.User;
import com.app.repo.UserRepository;
import com.app.session.UserSessionBean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GlobalInterceptor implements HandlerInterceptor {

    @Autowired
    private UserSessionBean userSessionBean;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // Publicly accessible endpoints
        if (path.startsWith("/login") || path.startsWith("/oauth2") ||
                path.startsWith("/css") || path.startsWith("/js") ||
                path.equals("/") || path.startsWith("/img") || path.startsWith("/banned")) {

            if ((path.startsWith("/login") || path.equals("/")) && userSessionBean.isLoggedIn()) {
                response.sendRedirect("/home");
                return false;
            }
            return true;
        }

        // Require login for everything else
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
}
