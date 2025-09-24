package com.alpha_code.alpha_code_user_service.controller;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.RoleDto;
import com.alpha_code.alpha_code_user_service.service.RoleService;
import com.alpha_code.alpha_code_user_service.validation.OnCreate;
import com.alpha_code.alpha_code_user_service.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles")
public class RoleController {

    private final RoleService service;

    @GetMapping

    @Operation(summary = "Get all roles with pagination")
    public PagedResult<RoleDto> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) Integer status) {
        return service.getAll(page, size, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by id")
    public RoleDto getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @Operation(summary = "Create new role")
    public RoleDto create(@Validated(OnCreate.class) @RequestBody RoleDto roleDto) {
        return service.create(roleDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    public RoleDto update(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody RoleDto dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @Operation(summary = "Patch update role")
    public RoleDto patchUpdate(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody RoleDto dto) {
        return service.patchUpdate(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_Admin')")
    @Operation(summary = "Delete role by id")
    public String delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
