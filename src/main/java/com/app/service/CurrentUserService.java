package com.app.service;

import com.app.model.User;
import com.app.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@Service
public class CurrentUserService {

    @Autowired
    private UserRepository userRepository;

    public OAuth2User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauthUser) {
            return oauthUser;
        }
        return null;
    }

    /** New helper: returns your app’s User entity or null if anonymous */
    public User getCurrentAppUser() {
        OAuth2User oAuth = getCurrentUser();
        if (oAuth == null) {
            System.out.println("DEBUG: No authenticated user found.");
            return null;
        }
        String email = oAuth.getAttribute("email");   // adjust key if provider differs
        return email == null ? null : userRepository.findByEmail(email).orElse(null);
    }

}
