package com.app.config;

import com.app.session.UserSessionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class to register interceptors for handling global and admin access.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserSessionBean userSessionBean;

    @Autowired
    private AdminAccessInterceptor adminAccessInterceptor;

    @Autowired
    private GlobalInterceptor globalInterceptor;

    /**
     * Configures the interceptors for the application.
     * <p>
     * The global interceptor is applied to all paths, while the admin access interceptor
     * is applied to all paths except static resources and OAuth2 endpoints.
     *
     * @param registry the interceptor registry to add interceptors to
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/oauth2/**", "/error"); // ✅ Add this

        registry.addInterceptor(adminAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/oauth2/**", "/error");
    }
}

