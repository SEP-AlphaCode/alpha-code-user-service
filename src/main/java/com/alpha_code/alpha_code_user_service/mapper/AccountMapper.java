package com.alpha_code.alpha_code_user_service.mapper;


import com.alpha_code.alpha_code_user_service.dto.AccountDto;
import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.enums.AccountEnum;
import user.User.AccountInformation;

public class AccountMapper {
    public static AccountDto toDto(Account account) {
        if (account == null) {
            return null;
        }
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setPassword(account.getPassword());
        dto.setFullName(account.getFullName());
        dto.setEmail(account.getEmail());
        dto.setPhone(account.getPhone());
        dto.setGender(account.getGender());
        dto.setImage(account.getImage());
        dto.setBannedReason(account.getBannedReason());
        dto.setCreatedDate(account.getCreatedDate());
        dto.setLastUpdated(account.getLastUpdated());
        dto.setRoleId(account.getRoleId());

        if (account.getRole() != null) {
            dto.setRoleName(account.getRole().getName());
        }

        dto.setLicenseId(account.getLicenseId());
        dto.setStatus(account.getStatus());
        return dto;
    }

    public static AccountInformation toAccountInformationGrpc(Account account) {
        if (account == null) {
            return null;
        }
        return AccountInformation.newBuilder()
                .setAccountId(account.getId().toString())
                .setEmail(account.getEmail())
                .setFullName(account.getFullName())
                .setPhone(account.getPhone())
                .setImage(account.getImage())
                .setGender(AccountEnum.fromCode(account.getGender()))
                .build();
    }

    public static Account toEntity(AccountDto dto) {
        if (dto == null) {
            return null;
        }
        Account account = new Account();
        account.setId(dto.getId());
        account.setUsername(dto.getUsername());
        account.setPassword(dto.getPassword());
        account.setFullName(dto.getFullName());
        account.setEmail(dto.getEmail());
        account.setPhone(dto.getPhone());
        account.setGender(dto.getGender());
        account.setCreatedDate(dto.getCreatedDate());
        account.setLastUpdated(dto.getLastUpdated());
        account.setImage(dto.getImage());
        account.setBannedReason(dto.getBannedReason());
        account.setRoleId(dto.getRoleId());
        account.setLicenseId(dto.getLicenseId());
        account.setStatus(dto.getStatus());
        return account;
    }
}

