package com.alpha_code.alpha_code_user_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestEnum {
    DELETED(0, "ĐÃ BỊ XÓA"),
    ACTIVE(1, "ĐÃ TẠO"),
    DONE(2, "ĐÃ TRẢ LỜI");

    private final int code;
    private final String description;

    public static String fromCode(Integer code) {
        if (code == null) return null;
        for (RequestEnum s : values()) {
            if (s.code == code) {
                return s.description;
            }
        }
        return "KHÔNG XÁC ĐỊNH";
    }
}
