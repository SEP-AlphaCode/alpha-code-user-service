package com.alpha_code.alpha_code_user_service.dto;

import com.alpha_code.alpha_code_user_service.enums.AccountEnum;
import com.alpha_code.alpha_code_user_service.validation.OnCreate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto implements Serializable {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotNull(message = "Tên không được để trống", groups = OnCreate.class)
    @Size(min = 1, max = 100, message = "Tên phải từ 1 đến 100 ký tự")
    private String name;

    private UUID accountId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountFullname;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String avatarUrl;

    private Boolean isKid = false;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID roleId;

    @NotNull(message = "ParentProfileId không được để trống", groups = OnCreate.class)
    private UUID parentProfileId;

    @NotNull(message = "PassCode không được để trống", groups = OnCreate.class)
    @Pattern(regexp = "^[0-9]{4}$", message = "PassCode phải gồm đúng 4 chữ số")
    @Size(max = 4, message = "PassCode không được dài quá 4 ký tự")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime lastActiveAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdDate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime lastUpdated;

    @NotNull(message = "Status không được để trống", groups = OnCreate.class)
    @Range(min = 0, max = 1, message = "Rate phải trong khoảng 0 đến 1")
    private Integer status;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile avatarFile;

    @JsonProperty(value = "statusText", access = JsonProperty.Access.READ_ONLY)
    public String getStatusText() {
        return AccountEnum.fromCode(this.status);
    }
}
