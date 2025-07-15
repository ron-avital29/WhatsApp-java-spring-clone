package com.app.security;

import com.app.model.User;
import com.app.repo.UserRepository;
import com.app.session.UserSessionBean;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * CustomOAuth2UserService handles the loading of user details from OAuth2 providers.
 * It checks if the user exists in the database, creates a new user if not,
 * and sets the user session attributes.
 */
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /**
     * Repository to access user data.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Password encoder to encode user passwords.
     */
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Session bean to manage user session data.
     */
    @Autowired
    private UserSessionBean userSessionBean;

    /**
     * HTTP request to access session attributes.
     */
    @Autowired
    private HttpServletRequest request;

    /**
     * Loads user details from the OAuth2 provider.
     * If the user does not exist, creates a new user with the provided details.
     * Sets the user session attributes and checks if the user is banned.
     *
     * @param oauthRequest the OAuth2 user request
     * @return an OAuth2User with the user's details
     * @throws OAuth2AuthenticationException if there is an error during authentication
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oauthRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(oauthRequest);

        String googleId = oauthUser.getAttribute("sub");   // Unique Google ID
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String avatar = oauthUser.getAttribute("picture");

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGoogleId(googleId);
                    newUser.setEmail(email);
                    newUser.setUsername(name);
                    newUser.setAvatarId(avatar);
                    newUser.setRole("USER");
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    return userRepository.save(newUser);
                });

        if (user.isBanned()) {
            request.getSession().setAttribute("bannedUntil", user.getBannedUntil());
            throw new LockedException("User is banned");
        }

        userSessionBean.setLoggedIn(true);
        userSessionBean.setUser(user);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                oauthUser.getAttributes(),
                "sub"
        );
    }
}
