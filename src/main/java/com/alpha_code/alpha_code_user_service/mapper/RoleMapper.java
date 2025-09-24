package com.alpha_code.alpha_code_user_service.mapper;


import com.alpha_code.alpha_code_user_service.dto.RoleDto;
import com.alpha_code.alpha_code_user_service.entity.Role;

public class RoleMapper {
    public static RoleDto toDto(Role role) {
        if (role == null) {
            return null;
        }
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setStatus(role.getStatus());
        return dto;
    }

    public static Role toEntity(RoleDto dto) {
        if (dto == null) {
            return null;
        }
        Role role = new Role();
        role.setId(dto.getId());
        role.setName(dto.getName());
        role.setStatus(dto.getStatus());
        return role;
    }
}
