package com.alpha_code.alpha_code_user_service.dto;

import com.alpha_code.alpha_code_user_service.enums.AccountEnum;
import com.alpha_code.alpha_code_user_service.enums.GenderEnum;
import com.alpha_code.alpha_code_user_service.validation.OnCreate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto implements Serializable {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotNull(message = "Tên đăng nhập không được để trống.", groups =  {OnCreate.class})
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự.")
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull(message = "Mật khẩu không được để trống.", groups =  {OnCreate.class})
    @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự.")
    private String password;

    @NotNull(message = "Họ và tên không được để trống.", groups =  {OnCreate.class})
    private String fullName;

    @NotNull(message = "Email không được để trống.", groups =  {OnCreate.class})
    @Email(message = "Email không hợp lệ.")
    private String email;

    @NotNull(message = "Số điện thoại không được để trống.", groups =  {OnCreate.class})
    @Size(min = 10, max = 11, message = "Số điện thoại phải từ 10 đến 11 ký tự.")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số.")
    private String phone;

    @NotNull(message = "Giới tính không được để trống.", groups =  {OnCreate.class})
    @Min(value = 0, message = "Giới tính phải là 0 hoặc 1.")
    @Max(value = 1, message = "Giới tính phải là 0 hoặc 1.")
    private Integer gender;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String image;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String bannedReason;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime lastUpdated;

    private UUID roleId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String roleName;

    private UUID licenseId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer status;

    @JsonProperty(value = "statusText", access = JsonProperty.Access.READ_ONLY)
    public String getStatusText() {
        return AccountEnum.fromCode(this.status);
    }

    @JsonProperty(value = "genderText", access = JsonProperty.Access.READ_ONLY)
    public String getGenderText() {
        return GenderEnum.fromCode(this.gender);
    }
}
