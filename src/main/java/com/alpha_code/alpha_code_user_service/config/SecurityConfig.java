package com.alpha_code.alpha_code_user_service.config;

import com.alpha_code.alpha_code_user_service.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
//                                .requestMatchers(
//                                        "/v3/api-docs/**",
//                                        "/swagger-ui.html",
//                                        "/swagger-ui/**",
//                                        "/api/v1/auth/**"
//                                ).permitAll()
////                        .anyRequest().authenticated()
//                                .anyRequest().permitAll()

                                // ✅ permit mọi method cho mấy endpoint chung
                                .requestMatchers(SecurityWhitelist.GENERAL_WHITELIST).permitAll()

                                // ✅ chỉ permit GET cho "get all"
                                .requestMatchers(HttpMethod.GET, SecurityWhitelist.GET_WHITELIST).permitAll()

                                // ❌ còn lại yêu cầu login
                                .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
//                .httpBasic(Customizer.withDefaults());
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("""
                        {"success":false,"error":"Unauthorized", "status":401,"message":"Invalid or missing token"}
                    """);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.getWriter().write("""
                        {"success":false,"error":"Forbidden", "status":403, "message":"Access Denied"}
                    """);
        };
    }
}
