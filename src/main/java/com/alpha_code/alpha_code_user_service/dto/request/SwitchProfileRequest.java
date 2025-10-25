package com.alpha_code.alpha_code_user_service.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class SwitchProfileRequest {
    private UUID accountId;
    private UUID profileId;
    private String passCode;
}
