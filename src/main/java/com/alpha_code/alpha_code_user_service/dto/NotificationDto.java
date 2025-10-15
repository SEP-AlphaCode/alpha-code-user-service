package com.alpha_code.alpha_code_user_service.dto;

import com.alpha_code.alpha_code_user_service.enums.NotificationStatusEnum;
import com.alpha_code.alpha_code_user_service.enums.NotificationTypeEnum;
import com.alpha_code.alpha_code_user_service.validation.OnCreate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto implements Serializable {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdDate;

    @NotNull(message = "Account ID không được để trống.", groups = {OnCreate.class})
    private UUID accountId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountFullName;

    @NotNull(message = "Loại thông báo không được để trống.", groups = {OnCreate.class})
    private Integer type;

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

    // Các field chỉ dùng khi tạo notification thanh toán — có thể null
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long orderCode;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String serviceName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer price;

    @JsonProperty(value = "statusText", access = JsonProperty.Access.READ_ONLY)
    public String getStatusText() {
        return NotificationStatusEnum.fromCode(this.status);
    }

    @JsonProperty(value = "typeText", access = JsonProperty.Access.READ_ONLY)
    public String getTypeText() {
        return NotificationTypeEnum.fromCode(this.type);
    }
}
