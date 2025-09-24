package com.alpha_code.alpha_code_user_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleEnum {
    DELETED(0, "ĐÃ BỊ XÓA"),
    ACTIVE(1, "HOẠT ĐỘNG");

    private final int code;
    private final String description;

    public static String fromCode(Integer code) {
        if (code == null) return null;
        for (RoleEnum s : values()) {
            if (s.code == code) {
                return s.description;
            }
        }
        return "KHÔNG XÁC ĐỊNH";
    }
}
