package com.app.service;

import com.app.model.User;
import com.app.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * CurrentUserService provides methods to retrieve the currently authenticated user
 * and the application user associated with that authentication.
 * It uses Spring Security's SecurityContext to access the current authentication details.
 */
@Service
public class CurrentUserService {

    /**
     * Repository to access user data.
     * It is used to find users by their email address.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the currently authenticated OAuth2 user.
     * It checks the SecurityContext for the current authentication and returns the OAuth2User if available.
     *
     * @return the current OAuth2User or null if not authenticated
     */
    public OAuth2User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauthUser) {
            return oauthUser;
        }
        return null;
    }

    /**
     * Retrieves the current application user based on the authenticated OAuth2 user.
     * It uses the email attribute from the OAuth2User to find the corresponding User in the repository.
     *
     * @return the current User or null if not authenticated or no user found
     */
    public User getCurrentAppUser() {
        OAuth2User oAuth = getCurrentUser();
        if (oAuth == null) {
            return null;
        }
        String email = oAuth.getAttribute("email");
        return email == null ? null : userRepository.findByEmail(email).orElse(null);
    }
}
