package com.alpha_code.alpha_code_user_service.dto;

import com.alpha_code.alpha_code_user_service.enums.AccountEnum;
import com.alpha_code.alpha_code_user_service.enums.GenderEnum;
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
public class AccountDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String fullName;

    private String email;

    private String phone;

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
