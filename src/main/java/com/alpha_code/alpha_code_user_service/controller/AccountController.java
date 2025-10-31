package com.alpha_code.alpha_code_user_service.controller;

import com.alpha_code.alpha_code_user_service.dto.AccountDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.service.AccountService;
import com.alpha_code.alpha_code_user_service.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts")
@Validated
public class AccountController {

    private final AccountService service;

    @GetMapping
    @Operation(summary = "Get all accounts with pagination and optional status filter")
    public PagedResult<AccountDto> getAll(@RequestParam(value = "page", defaultValue = "1") int page,
                                          @RequestParam(value = "size", defaultValue = "10") int size,
                                          @RequestParam(value = "status", required = false) Integer status) {
        return service.getAll(page, size, status);
    }

    @GetMapping("/full-name")
    @Operation(summary = "Find account by full name")
    public AccountDto findAccountByFullName(@RequestParam String fullName) {
        return service.findAccountByFullName(fullName);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by id")
    public AccountDto getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new account")
//    @PreAuthorize("hasAuthority('ROLE_Admin')")
    public AccountDto create(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            @RequestParam("username") String username,

            @NotBlank(message = "Password is required")
            @Size(min = 6, message = "Password must be at least 6 characters long")
            @RequestParam("password") String password,

            @NotBlank(message = "Full name is required")
            @RequestParam("fullName") String fullName,

            @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Invalid phone number format")
            @RequestParam("phone") String phone,

            @NotBlank(message = "Email is required")
            @Email(message = "Email should be valid")
            @RequestParam("email") String email,

            @Min(value = 0, message = "Gender invalid (0=Unknown, 1=Male, 2=Female)")
            @Max(value = 2, message = "Gender invalid (0=Unknown, 1=Male, 2=Female)")
            @RequestParam("gender") Integer gender,

            @NotNull(message = "Role ID is required")
            @RequestParam("roleId") UUID roleId,

            @RequestPart(value = "avatarFile") MultipartFile avatarFile) {

        AccountDto accountDto = new AccountDto();
        accountDto.setUsername(username);
        accountDto.setPassword(password);
        accountDto.setFullName(fullName);
        accountDto.setPhone(phone);
        accountDto.setEmail(email);
        accountDto.setGender(gender);
        accountDto.setRoleId(roleId);
        return service.create(accountDto, avatarFile);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update account by id")
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    public AccountDto update(@PathVariable UUID id, @Valid @RequestBody AccountDto dto) {
        return service.update(id, dto);
    }

    @PutMapping(value = "/{id}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update account profile")
    @PreAuthorize("hasAnyAuthority('ROLE_Admin', 'ROLE_Staff')")
    public AccountDto updateProfile(
            @PathVariable UUID id,

            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            @RequestParam("username") String username,

            @NotBlank(message = "Password is required")
            @Size(min = 6, message = "Password must be at least 6 characters long")
            @RequestParam("password") String password,

            @NotBlank(message = "Full name is required")
            @RequestParam("fullName") String fullName,

            @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Invalid phone number format")
            @RequestParam("phone") String phone,

            @NotBlank(message = "Email is required")
            @Email(message = "Email should be valid")
            @RequestParam("email") String email,

            @Min(value = 0, message = "Gender invalid (0=Unknown, 1=Male, 2=Female)")
            @Max(value = 2, message = "Gender invalid (0=Unknown, 1=Male, 2=Female)")
            @RequestParam("gender") Integer gender,

            @NotNull(message = "Role ID is required")
            @RequestParam("roleId") UUID roleId,
            @RequestPart(value = "avatarFile") MultipartFile avatarFile) {
        AccountDto accountDto = new AccountDto();
        accountDto.setId(id);
        accountDto.setUsername(username);
        accountDto.setPassword(password);
        accountDto.setFullName(fullName);
        accountDto.setPhone(phone);
        accountDto.setEmail(email);
        accountDto.setGender(gender);
        accountDto.setRoleId(roleId);
        return service.updateProfile(id, accountDto, avatarFile);
    }

    @PatchMapping(value = "/{id}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Patch update account profile")
    @PreAuthorize("hasAnyAuthority('ROLE_Admin', 'ROLE_Staff')")
    public AccountDto patchUpdateProfile(
            @PathVariable UUID id,

            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            @RequestParam(value = "username", required = false) String username,

            @Size(min = 6, message = "Password must be at least 6 characters long")
            @RequestParam(value = "password", required = false) String password,

            @RequestParam(value = "fullName", required = false) String fullName,

            @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Invalid phone number format")
            @RequestParam(value = "phone", required = false) String phone,

            @Email(message = "Email should be valid")
            @RequestParam(value = "email", required = false) String email,

            @Min(value = 0, message = "Gender invalid (0=Unknown, 1=Male, 2=Female)")
            @Max(value = 2, message = "Gender invalid (0=Unknown, 1=Male, 2=Female)")
            @RequestParam(value = "gender", required = false) Integer gender,

            @RequestParam(value = "roleId", required = false) UUID roleId,

            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {
        AccountDto accountDto = new AccountDto();
        if (username != null) {
            accountDto.setUsername(username);
        }
        if (password != null) {
            accountDto.setPassword(password);
        }
        if (fullName != null) {
            accountDto.setFullName(fullName);
        }
        if (phone != null) {
            accountDto.setPhone(phone);
        }
        if (email != null) {
            accountDto.setEmail(email);
        }
        if (gender != null) {
            accountDto.setGender(gender);
        }
        if (roleId != null) {
            accountDto.setRoleId(roleId);
        }
        return service.patchUpdateProfile(id, accountDto, avatarFile);
    }




    @PatchMapping("/{id}")
    @Operation(summary = "Patch update account by id")
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    public AccountDto patchUpdate(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody AccountDto dto) {
        return service.patchUpdate(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account by id")
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    public String delete(@PathVariable UUID id) {
        return service.delete(id);
    }

    @PatchMapping("/{id}/change-password")
    @Operation(summary = "Change password")
    public AccountDto changePassword(@PathVariable UUID id,
                                     @RequestParam String oldPassword,
                                     @RequestParam String newPassword) {
        return service.changePassword(id, oldPassword, newPassword);
    }

    @PatchMapping("/{id}/change-status")
    @Operation(summary = "Change account status")
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    public AccountDto changeStatus(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestParam Integer status, @RequestParam(required = false) String bannedReason) {
        return service.changeStatus(id, status, bannedReason);
    }
}
