package com.alpha_code.alpha_code_user_service.service;


import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.RoleDto;

import java.util.UUID;

public interface RoleService {
    PagedResult<RoleDto> getAll(int page, int size, Integer status);

    RoleDto getById(UUID id);

    RoleDto create(RoleDto roleDto);

    RoleDto update(UUID id, RoleDto roleDto);

    RoleDto patchUpdate(UUID id, RoleDto roleDto);

    String delete(UUID id);

    RoleDto changeStatus(UUID id, Integer status);
}
