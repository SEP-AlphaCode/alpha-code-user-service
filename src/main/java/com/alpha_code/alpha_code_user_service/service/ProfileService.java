package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.LoginDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.ProfileDto;
import com.alpha_code.alpha_code_user_service.dto.request.SwitchProfileRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProfileService {
    PagedResult<ProfileDto> searchProfiles(int page, int size, String name, UUID accountId, Integer status, Boolean isKid, Integer passCode);

    ProfileDto getProfileById(UUID id);

    ProfileDto createProfile(ProfileDto profileDto);

    ProfileDto updateProfile(UUID id, ProfileDto profileDto);

    ProfileDto patchUpdateProfile(ProfileDto profileDto);

    ProfileDto updateProfileWithImage(ProfileDto profileDto, MultipartFile profileImage);

    ProfileDto patchUpdateProfileWithImage(ProfileDto profileDto, MultipartFile profileImage);

    void deleteProfile(UUID id);

    ProfileDto updateProfileStatus(UUID id, Integer status);

    ProfileDto updateProfilePassCode(UUID id, Integer oldPassCode, Integer passCode);

    ProfileDto updateAvatar(UUID id, MultipartFile avatar);

    List<ProfileDto> getByAccountId(UUID accountId);
}
