package com.alpha_code.alpha_code_user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.mail.Multipart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class LoginDto implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponse {
        private Boolean requiresProfile;
        private List<ProfileDto> profiles;
        private String accessToken;
        private String refreshToken;
        private UUID accountId;
        private String key;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        private String username;
        private String password;
        private String fullName;
        private String email;
        private String phone;
        private Integer gender;

        @JsonProperty(access =  JsonProperty.Access.WRITE_ONLY)
        private MultipartFile avatarFile;
    }
}

