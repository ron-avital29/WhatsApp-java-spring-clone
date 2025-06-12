package com.app.service;

import com.app.model.User;
import com.app.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a user via traditional signup (e.g., with password).
     */
    public User register(User user) {
        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default role
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    /**
     * Logs in or registers a user from Google OAuth login.
     * Uses googleId (unique) to determine identity.
     */
    public User findOrCreateGoogleUser(String googleId, String email, String displayName, String avatarId) {
        return userRepository.findByGoogleId(googleId).orElseGet(() -> {
            User user = new User();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setUsername(displayName); // Not unique!
            user.setAvatarId(avatarId);
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());
            return userRepository.save(user);
        });
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }
}
