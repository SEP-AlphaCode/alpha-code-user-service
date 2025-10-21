package com.alpha_code.alpha_code_user_service.mapper;

import com.alpha_code.alpha_code_user_service.dto.ProfileDto;
import com.alpha_code.alpha_code_user_service.entity.Profile;

public class ProfileMapper {
    public static ProfileDto toDto(Profile profile) {
        if (profile == null) {
            return null;
        }

        ProfileDto dto = new ProfileDto();
        dto.setId(profile.getId());
        dto.setName(profile.getName());
        dto.setAccountId(profile.getAccountId());
        if (profile.getAccount() != null) {
            dto.setAccountFullname(profile.getAccount().getFullName());
        }
        dto.setRoleId(profile.getRoleId());
        if (profile.getRole() != null){
            dto.setRoleName(profile.getRole().getName());
        }
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setIsKid(profile.getIsKid());
        dto.setPassCode(profile.getPassCode());
        dto.setLastActiveAt(profile.getLastActiveAt());
        dto.setCreatedDate(profile.getCreatedDate());
        dto.setLastUpdated(profile.getLastUpdated());
        dto.setStatus(profile.getStatus());
        return dto;

    }

    public static Profile toEntity(ProfileDto dto) {
        if (dto == null) {
            return null;
        }

        Profile profile = new Profile();
        profile.setId(dto.getId());
        profile.setName(dto.getName());
        profile.setAccountId(dto.getAccountId());
        profile.setRoleId(dto.getRoleId());
        profile.setAvatarUrl(dto.getAvatarUrl());
        profile.setIsKid(dto.getIsKid());
        profile.setPassCode(dto.getPassCode());
        profile.setLastActiveAt(dto.getLastActiveAt());
        profile.setCreatedDate(dto.getCreatedDate());
        profile.setLastUpdated(dto.getLastUpdated());
        profile.setStatus(dto.getStatus());
        return profile;
    }
}
