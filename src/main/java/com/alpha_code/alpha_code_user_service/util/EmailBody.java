package com.alpha_code.alpha_code_user_service.util;

public class EmailBody {
    public static String getResetPasswordEmailBody(String fullName, String resetLink) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 0;">
                    <table align="center" width="100%" cellpadding="0" cellspacing="0" 
                           style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 12px; 
                                  overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                        <tr style="background-color: #2b2d42;">
                            <td align="center" style="padding: 24px;">
                                <img src="cid:alphacode-logo" alt="alphaCode Logo" width="80" 
                                     style="display:block; margin-bottom: 12px;" />
                                <h1 style="color: #ffffff; font-size: 20px; margin: 0;">AlphaCode</h1>
                                <p style="color: #dce0e6; font-size: 14px; margin: 4px 0 0;">
                                    Nền tảng quản lý robot Alpha Mini
                                </p>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding: 32px;">
                                <h2 style="color: #2b2d42; font-size: 22px; margin: 0 0 16px;">Đặt lại mật khẩu</h2>
                                <p style="color: #444; font-size: 15px; line-height: 1.6; margin: 0 0 24px;">
                                    Xin chào <b>{fullName}</b>,<br><br>
                                    Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. 
                                    Vui lòng nhấn vào nút bên dưới để tiếp tục:
                                </p>
                                <div style="text-align: center; margin: 24px 0;">
                                    <a href="{resetLink}" 
                                       style="background-color: #ef233c; color: #ffffff; text-decoration: none; 
                                              padding: 12px 24px; border-radius: 8px; font-size: 16px; 
                                              font-weight: bold; display: inline-block;">
                                        Đặt lại mật khẩu
                                    </a>
                                </div>
                                <p style="color: #777; font-size: 14px; line-height: 1.6; margin: 0;">
                                    Nếu bạn không yêu cầu thay đổi này, vui lòng bỏ qua email này.<br>
                                    Vì lý do bảo mật, đường dẫn sẽ hết hạn sau một khoảng thời gian.
                                </p>
                            </td>
                        </tr>
                        <tr style="background-color: #f5f7fa;">
                            <td align="center" style="padding: 16px; font-size: 12px; color: #999;">
                                © 2025 alphaCode. All rights reserved.<br>
                                Nền tảng quản lý robot thông minh Alpha Mini.
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .replace("{fullName}", fullName)
                .replace("{resetLink}", resetLink);
    }
}
