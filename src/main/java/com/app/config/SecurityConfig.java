package com.app.config;

import com.app.security.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * Security configuration for the application.
 * This class configures security settings, including OAuth2 login, CSRF protection, and access control.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the security filter chain for the application.
     *
     * @param http the HttpSecurity object to configure
     * @param oAuth2UserService the custom OAuth2 user service
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService oAuth2UserService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/login", "/error", "/banned").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .defaultSuccessUrl("/home", true)
                        .failureHandler(authenticationFailureHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/logout-success")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Provides a BCryptPasswordEncoder bean for password encoding.
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Custom authentication failure handler that redirects users based on the type of authentication failure.
     *
     * @return an AuthenticationFailureHandler instance
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException exception) -> {
            if (exception instanceof LockedException) {
                response.sendRedirect("/banned");
            } else {
                response.sendRedirect("/login");
            }
        };
    }
}
