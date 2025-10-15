package com.alpha_code.alpha_code_user_service.util;

public class PaymentNotification {
    public static String getMessage(Long orderCode, String serviceName, Integer price) {
        return String.format(
                "Cảm ơn bạn đã mua gói dịch vụ %s!\nMã đơn hàng: #%d\nGiá trị: %,d VND",
                serviceName, orderCode, price
        );
    }

    public static String getTitle(String serviceName) {
        return String.format("Thanh toán thành công gói %s", serviceName);
    }
}
