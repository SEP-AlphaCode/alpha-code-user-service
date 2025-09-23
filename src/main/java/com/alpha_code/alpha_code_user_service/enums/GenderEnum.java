package com.alpha_code.alpha_code_user_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderEnum {
    UNKNOWN(0, "UNKNOWN"),
    MALE(1, "MALE"),
    FEMALE(2, "FEMALE");

    private final int code;
    private final String description;

    public static String fromCode(Integer code) {
        if (code == null) return null;
        for (GenderEnum g : values()) {
            if (g.code == code) {
                return g.description;
            }
        }
        return "UNDEFINED";
    }
}
