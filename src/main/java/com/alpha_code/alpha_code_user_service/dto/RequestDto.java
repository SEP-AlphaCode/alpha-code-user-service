package com.alpha_code.alpha_code_user_service.dto;

import com.alpha_code.alpha_code_user_service.enums.RoleEnum;
import com.alpha_code.alpha_code_user_service.enums.RequestEnum;
import com.alpha_code.alpha_code_user_service.validation.OnCreate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto implements Serializable {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotNull(message = "Account ID không được để trống", groups = {OnCreate.class})
    private UUID accountId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String accountFullName;

    @NotNull(message = "Title không được để trống", groups =  {OnCreate.class})
    private String title;

    @NotNull(message = "Description không được để trống", groups = {OnCreate.class})
    private String description;

    @NotNull(message = "Type không được để trống", groups = {OnCreate.class})
    private String type;

    @Range(min = 0, max = 5, message = "Rate phải trong khoảng 0 đến 5")
    private Integer rate;

    private Integer status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdDated;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime lastUpdated;

    @JsonProperty(value = "statusText", access = JsonProperty.Access.READ_ONLY)
    public String getStatusText() {
        return RequestEnum.fromCode(this.status);
    }
}
