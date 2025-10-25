package com.alpha_code.alpha_code_user_service.controller;

import com.alpha_code.alpha_code_user_service.dto.LoginDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.ProfileDto;
import com.alpha_code.alpha_code_user_service.dto.request.SwitchProfileRequest;
import com.alpha_code.alpha_code_user_service.service.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public PagedResult<ProfileDto> getAll(@RequestParam(value = "page", defaultValue = "1") int page,
                                          @RequestParam(value = "size", defaultValue = "10") int size,
                                          @RequestParam(value = "name", required = false) String name,
                                          @RequestParam(value = "accountId", required = false) UUID accountId,
                                          @RequestParam(value = "status", required = false) Integer status,
                                          @RequestParam(value = "isKid", required = false) Boolean isKid,
                                          @RequestParam(value = "passCode", required = false) String passCode) {
        return profileService.searchProfiles(page, size, name, accountId, status, isKid, passCode);
    }

    @GetMapping("/{id}")
    public ProfileDto getProfileById(@PathVariable UUID id) {
        return profileService.getProfileById(id);
    }

    @GetMapping("/by-account/{accountId}")
    public List<ProfileDto> getByAccountId(@PathVariable UUID accountId) {
        return profileService.getByAccountId(accountId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDto createProfile(@ModelAttribute ProfileDto profileDto) {
        return profileService.createProfile(profileDto);
    }

    @PutMapping("/{id}")
    public ProfileDto updateProfile(@PathVariable UUID id, @RequestBody ProfileDto profileDto) {
        return profileService.updateProfile(id, profileDto);
    }

    @PutMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileDto updateAvatar(@PathVariable UUID id, @RequestPart MultipartFile avatar) {
        return profileService.updateAvatar(id, avatar);
    }

    @DeleteMapping("/{id}")
    public void deleteProfile(@PathVariable UUID id) {
        profileService.deleteProfile(id);
    }


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                // Khi form không có file, Spring sẽ set text="" → ta chuyển về null
                setValue(null);
            }
        });
    }
}
