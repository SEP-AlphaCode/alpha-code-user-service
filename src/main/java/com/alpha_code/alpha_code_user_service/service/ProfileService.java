package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.ProfileDto;
import jakarta.mail.Multipart;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProfileService {
    PagedResult<ProfileDto> searchProfiles(int page, int size, String name, UUID accountId, Integer status, Boolean isKid, Integer passCode);

    ProfileDto getProfileById(UUID id);

    ProfileDto createProfile(ProfileDto profileDto);

    ProfileDto updateProfile(ProfileDto profileDto);

    ProfileDto patchUpdateProfile(ProfileDto profileDto);

    ProfileDto updateProfileWithImage(ProfileDto profileDto, MultipartFile profileImage);

    ProfileDto patchUpdateProfileWithImage(ProfileDto profileDto, MultipartFile profileImage);

    String deleteProfile(UUID id);

    ProfileDto updateProfileStatus(UUID id, Integer status);

    ProfileDto updateProfilePassCode(UUID id, Integer oldPassCode, Integer passCode);
}
