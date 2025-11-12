package com.alpha_code.alpha_code_user_service.config;

import org.springframework.stereotype.Component;

@Component
public class SecurityWhitelist {

    // Permit all methods
    public static final String[] GENERAL_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger",
            "/docs",
            "/",
            "/api/v1/auth/**",
            "/ws/**"
    };

    // Permit GET only
    public static final String[] GET_WHITELIST = {
            "/api/v1/**"
    };

    public static final String[] POST_WHITELIST = {
            "/api/v1/profiles"
    };
}