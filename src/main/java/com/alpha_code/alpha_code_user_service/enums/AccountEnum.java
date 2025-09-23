package com.alpha_code.alpha_code_user_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountEnum {
    DELETED(0, "DELETED"),
    ACTIVE(1, "ACTIVE"),
    BANNED(2, "BANNED");

    private final int code;
    private final String description;

    public static String fromCode(Integer code) {
        if (code == null) return null;
        for (AccountEnum s : values()) {
            if (s.code == code) {
                return s.description;
            }
        }
        return "UNDEFINED";
    }
}
