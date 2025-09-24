package com.alpha_code.alpha_code_user_service.dto;

import com.alpha_code.alpha_code_user_service.enums.NotificationEnum;
import com.alpha_code.alpha_code_user_service.enums.RoleEnum;
import com.alpha_code.alpha_code_user_service.validation.OnCreate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdDate;

    @NotNull(message = "Account ID không được để trống.", groups = {OnCreate.class})
    private UUID accountId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountFullName;

    @NotNull(message = "Loại thông báo không được để trống.", groups = {OnCreate.class})
    private String type;

    @NotNull(message = "Tiêu đề không được để trống.", groups = {OnCreate.class})
    private String title;

    @NotNull(message = "Nội dung không được để trống.", groups = {OnCreate.class})
    private String message;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean isRead = false;

    @NotNull(message = "Trạng thái không được để trống.", groups = {OnCreate.class})
    private Integer status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime lastUpdated;

    @JsonProperty(value = "statusText", access = JsonProperty.Access.READ_ONLY)
    public String getStatusText() {
        return NotificationEnum.fromCode(this.status);
    }
}
