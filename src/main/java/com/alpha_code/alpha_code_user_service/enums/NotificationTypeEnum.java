package com.alpha_code.alpha_code_user_service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTypeEnum {
    //Type enum
    PAYMENT_SUCCESS(1, "Thanh toán thành công"),
    COURSE_ASSIGNED(2, "Khóa học mới được thêm"),
    LICENSE_EXPIRED(3, "Giấy phép sắp hết hạn"),
    SYSTEM(4, "Thông báo hệ thống"),
    PROMOTION(5, "Khuyến mãi");

    private final int code;
    private final String description;

    public static String fromCode(Integer code) {
        if (code == null) return null;
        for (NotificationTypeEnum s : values()) {
            if (s.code == code) {
                return s.description;
            }
        }
        return "KHÔNG XÁC ĐỊNH";
    }

    public static NotificationTypeEnum fromCodeValue(Integer code) {
        if (code == null) return null;
        for (NotificationTypeEnum s : values()) {
            if (s.code == code) return s;
        }
        return null;
    }
}
