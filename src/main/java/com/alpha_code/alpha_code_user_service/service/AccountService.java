package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.AccountDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.entity.Account;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AccountService {
    PagedResult<AccountDto> getAll(int page, int size, Integer status);

    AccountDto getById(UUID id);

    AccountDto create(AccountDto accountDto, MultipartFile avatarFile);

    AccountDto update(UUID id, AccountDto accountDto);

    AccountDto updateProfile(UUID id, AccountDto accountDto, MultipartFile avatarFile);

    AccountDto patchUpdate(UUID id, AccountDto accountDto);

    AccountDto patchUpdateProfile(UUID id, AccountDto accountDto, MultipartFile avatarFile);

    AccountDto changePassword(UUID id, String oldPassword, String newPassword);

    AccountDto changeStatus(UUID id, Integer status);

    String delete(UUID id);

    AccountDto findAccountByFullName(String fullName);

    Account findAccountByIdGrpc(UUID id);
}
