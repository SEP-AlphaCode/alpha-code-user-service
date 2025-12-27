package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.ProfileDto;
import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.entity.Profile;
import com.alpha_code.alpha_code_user_service.entity.Role;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.ProfileRepository;
import com.alpha_code.alpha_code_user_service.repository.RoleRepository;
import com.alpha_code.alpha_code_user_service.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private Profile profile;
    private ProfileDto profileDto;
    private Account account;
    private Role role;
    private UUID profileId;
    private UUID accountId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        role = new Role();
        role.setId(roleId);
        role.setName("Parent");

        account = new Account();
        account.setId(accountId);
        account.setUsername("testuser");
        account.setFullName("Test User");

        profile = new Profile();
        profile.setId(profileId);
        profile.setAccountId(accountId);
        profile.setName("Test Profile");
        profile.setIsKid(false);
        profile.setStatus(1);
        profile.setRoleId(roleId);
        profile.setPassCode("encodedPassCode");
        profile.setCreatedDate(LocalDateTime.now());

        profileDto = new ProfileDto();
        profileDto.setId(profileId);
        profileDto.setAccountId(accountId);
        profileDto.setName("Test Profile");
        profileDto.setIsKid(false);
        profileDto.setStatus(1);
        profileDto.setPassCode("passCode123");
    }

    @Test
    void testSearchProfiles() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Profile> page = new PageImpl<>(List.of(profile), pageable, 1);

        when(profileRepository.searchProfiles(null, null, null, null, null, pageable)).thenReturn(page);

        var result = profileService.searchProfiles(1, 10, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        verify(profileRepository).searchProfiles(null, null, null, null, null, pageable);
    }

    @Test
    void testGetProfileById_Success() {
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));

        var result = profileService.getProfileById(profileId);

        assertNotNull(result);
        assertEquals(profileId, result.getId());
        verify(profileRepository).findById(profileId);
    }

    @Test
    void testGetProfileById_NotFound() {
        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> profileService.getProfileById(profileId));
    }

    @Test
    void testCreateProfile_Success() throws Exception {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(roleRepository.findByNameIgnoreCase("Parent")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("passCode123")).thenReturn("encodedPassCode");
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        var result = profileService.createProfile(profileDto);

        assertNotNull(result);
        verify(accountRepository).findById(accountId);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testCreateProfile_AccountNotFound() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> profileService.createProfile(profileDto));
    }

    @Test
    void testUpdateProfile_Success() {
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(roleRepository.findByNameIgnoreCase("Parent")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("passCode123")).thenReturn("encodedPassCode");
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        var result = profileService.updateProfile(profileId, profileDto);

        assertNotNull(result);
        verify(profileRepository).findById(profileId);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testPatchUpdateProfile_Success() {
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(roleRepository.findByNameIgnoreCase("Parent")).thenReturn(Optional.of(role));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        var result = profileService.patchUpdateProfile(profileDto);

        assertNotNull(result);
        verify(profileRepository).findById(profileId);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testDeleteProfile_Success() {
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        profileService.deleteProfile(profileId);

        verify(profileRepository).findById(profileId);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testUpdateProfileStatus_Success() {
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        var result = profileService.updateProfileStatus(profileId, 0);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        verify(profileRepository).findById(profileId);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testUpdateProfilePassCode_Success() {
        profile.setPassCode("oldEncodedPassCode");
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(passwordEncoder.encode("newPassCode")).thenReturn("newEncodedPassCode");
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        var result = profileService.updateProfilePassCode(profileId, "oldEncodedPassCode", "newPassCode");

        assertNotNull(result);
        verify(profileRepository).findById(profileId);
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testUpdateProfilePassCode_WrongOldPassCode() {
        profile.setPassCode("oldEncodedPassCode");
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));

        assertThrows(RuntimeException.class, 
            () -> profileService.updateProfilePassCode(profileId, "wrongOldPassCode", "newPassCode"));
    }

    @Test
    void testUpdateAvatar_Success() throws Exception {
        MultipartFile avatarFile = mock(MultipartFile.class);
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(avatarFile.isEmpty()).thenReturn(false);
        when(avatarFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(avatarFile.getOriginalFilename()).thenReturn("avatar.jpg");
        when(avatarFile.getContentType()).thenReturn("image/jpeg");
        when(s3Service.uploadBytes(any(), any(), any())).thenReturn("https://s3.url/avatar.jpg");
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        var result = profileService.updateAvatar(profileId, avatarFile);

        assertNotNull(result);
        verify(s3Service).uploadBytes(any(), any(), any());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void testGetByAccountId() {
        when(profileRepository.findListByAccountIdAndStatus(accountId, 1)).thenReturn(List.of(profile));

        var result = profileService.getByAccountId(accountId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(profileRepository).findListByAccountIdAndStatus(accountId, 1);
    }
}

