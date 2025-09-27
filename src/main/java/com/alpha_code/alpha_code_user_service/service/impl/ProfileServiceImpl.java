package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.ProfileDto;
import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.entity.Profile;
import com.alpha_code.alpha_code_user_service.mapper.AccountMapper;
import com.alpha_code.alpha_code_user_service.mapper.ProfileMapper;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.ProfileRepository;
import com.alpha_code.alpha_code_user_service.service.ProfileService;
import com.alpha_code.alpha_code_user_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final AccountRepository accountRepository;
    private final S3Service s3Service;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "profiles_list", key = "{#page, #size, #name, #accountId, #status, #isKid, #passCode}")
    public PagedResult<ProfileDto> searchProfiles(int page, int size, String name, UUID accountId, Integer status, Boolean isKid, Integer passCode) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Profile> pageResult;

        pageResult = profileRepository.searchProfiles(name, accountId, status, isKid, passCode, pageable);

        return new PagedResult<>(pageResult.map(ProfileMapper::toDto));

    }

    @Override
    @Cacheable(value = "profile", key = "#id")
    public ProfileDto getProfileById(UUID id) {
        var profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return ProfileMapper.toDto(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"profiles_list, profile"}, allEntries = true)
    public ProfileDto createProfile(ProfileDto profileDto) {
        var profile = ProfileMapper.toEntity(profileDto);
        var account = accountRepository.findById(profileDto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        profile.setAccount(account);
        profile.setCreatedDate(LocalDateTime.now());

        try {
            if (profileDto.getAvatarFile() != null && !profileDto.getAvatarFile().isEmpty()) {
                String fileKey = "avatars/" + System.currentTimeMillis() + "_" + profileDto.getAvatarFile().getOriginalFilename();
                String avatarUrl = s3Service.uploadBytes(profileDto.getAvatarFile().getBytes(), fileKey, profileDto.getAvatarFile().getContentType());
                profile.setAvatarUrl(avatarUrl);
            }

            Profile savedEntity = profileRepository.save(profile);
            return ProfileMapper.toDto(savedEntity);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tải Ảnh đại diện", e);
        }

    }

    @Override
    @Transactional
    @CachePut(value = "profile", key = "#profileDto.id")
    @CacheEvict(value = "profiles_list", allEntries = true)
    public ProfileDto updateProfile(ProfileDto profileDto) {
        var profile = profileRepository.findById(profileDto.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setName(profileDto.getName());
        profile.setAccountId(profileDto.getAccountId());
        profile.setIsKid(profileDto.getIsKid());
        profile.setPassCode(profileDto.getPassCode());
        profile.setLastActiveAt(profileDto.getLastActiveAt());
        profile.setStatus(profileDto.getStatus());

        profile.setLastUpdated(LocalDateTime.now());

        Profile savedEntity = profileRepository.save(profile);
        return ProfileMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CachePut(value = "profile", key = "#profileDto.id")
    @CacheEvict(value = "profiles_list", allEntries = true)
    public ProfileDto patchUpdateProfile(ProfileDto profileDto) {
        var profile = profileRepository.findById(profileDto.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profileDto.getName() != null){
            profile.setName(profileDto.getName());
        }
        if (profileDto.getAccountId() != null){
            profile.setAccountId(profileDto.getAccountId());
        }
        if (profileDto.getIsKid() != null){
            profile.setIsKid(profileDto.getIsKid());
        }
        if (profileDto.getPassCode() != null){
            profile.setPassCode(profileDto.getPassCode());
        }
        if (profileDto.getLastActiveAt() != null){
            profile.setLastActiveAt(profileDto.getLastActiveAt());
        }
        if (profileDto.getStatus() != null){
            profile.setStatus(profileDto.getStatus());
        }

        profile.setLastUpdated(LocalDateTime.now());

        Profile savedEntity = profileRepository.save(profile);
        return ProfileMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CachePut(value = "profile", key = "#profileDto.id")
    @CacheEvict(value = "profiles_list", allEntries = true)
    public ProfileDto updateProfileWithImage(ProfileDto profileDto, MultipartFile profileImage) {
        var profile = profileRepository.findById(profileDto.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setName(profileDto.getName());
        profile.setAccountId(profileDto.getAccountId());
        profile.setIsKid(profileDto.getIsKid());
        profile.setPassCode(profileDto.getPassCode());
        profile.setLastActiveAt(profileDto.getLastActiveAt());
        profile.setStatus(profileDto.getStatus());

        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                String fileKey = "avatars/" + System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
                String avatarUrl = s3Service.uploadBytes(profileImage.getBytes(), fileKey, profileImage.getContentType());
                profile.setAvatarUrl(avatarUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tải Ảnh đại diện", e);
        }

        profile.setLastUpdated(LocalDateTime.now());

        Profile savedEntity = profileRepository.save(profile);
        return ProfileMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CachePut(value = "profile", key = "#profileDto.id")
    @CacheEvict(value = "profiles_list", allEntries = true)
    public ProfileDto patchUpdateProfileWithImage(ProfileDto profileDto, MultipartFile profileImage) {
        var profile = profileRepository.findById(profileDto.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profileDto.getName() != null){
            profile.setName(profileDto.getName());
        }
        if (profileDto.getAccountId() != null){
            profile.setAccountId(profileDto.getAccountId());
        }
        if (profileDto.getIsKid() != null){
            profile.setIsKid(profileDto.getIsKid());
        }
        if (profileDto.getPassCode() != null){
            profile.setPassCode(profileDto.getPassCode());
        }
        if (profileDto.getLastActiveAt() != null){
            profile.setLastActiveAt(profileDto.getLastActiveAt());
        }
        if (profileDto.getStatus() != null){
            profile.setStatus(profileDto.getStatus());
        }

        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                String fileKey = "avatars/" + System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
                String avatarUrl = s3Service.uploadBytes(profileImage.getBytes(), fileKey, profileImage.getContentType());
                profile.setAvatarUrl(avatarUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tải Ảnh đại diện", e);
        }

        profile.setLastUpdated(LocalDateTime.now());

        Profile savedEntity = profileRepository.save(profile);
        return ProfileMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CachePut(value = "profile", key = "#profileDto.id")
    @CacheEvict(value = "profiles_list", allEntries = true)
    public String deleteProfile(UUID id) {
        var profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setStatus(0);
        profile.setLastUpdated(LocalDateTime.now());

        profileRepository.save(profile);
        return "Xóa thành công Profile với id: " + id;
    }

    @Override
    @Transactional
    @CachePut(value = "profile", key = "#profileDto.id")
    @CacheEvict(value = "profiles_list", allEntries = true)
    public ProfileDto updateProfileStatus(UUID id, Integer status) {
        var profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setStatus(status);
        profile.setLastUpdated(LocalDateTime.now());

        Profile savedEntity = profileRepository.save(profile);
        return ProfileMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CachePut(value = "profile", key = "#profileDto.id")
    @CacheEvict(value = "profiles_list", allEntries = true)
    public ProfileDto updateProfilePassCode(UUID id, Integer oldPassCode, Integer passCode) {
        var profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (!profile.getPassCode().equals(oldPassCode)) {
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }

        profile.setPassCode(passCode);
        profile.setLastUpdated(LocalDateTime.now());

        Profile savedEntity = profileRepository.save(profile);
        return  ProfileMapper.toDto(savedEntity);
    }
}
