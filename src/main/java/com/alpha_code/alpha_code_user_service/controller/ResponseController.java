package com.alpha_code.alpha_code_user_service.controller;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.ResponseDto;
import com.alpha_code.alpha_code_user_service.service.ResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/response")
@RequiredArgsConstructor
@Tag(name = "User Requests")
@Validated
public class ResponseController {

    private final ResponseService service;

    @GetMapping
    @Operation(summary = "Get all responses with pagination and optional filters")
    public PagedResult<ResponseDto> getAllResponse(@RequestParam(value = "page", defaultValue = "1") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size,
                                                   @RequestParam(value = "status", required = false) Integer status,
                                                   @RequestParam(value = "requestId", required = false) UUID requestId,
                                                   @RequestParam(value = "responderId", required = false) UUID responderId) {
        return service.getAll(page, size, status, requestId, responderId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get response by id")
    public ResponseDto getResponseById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create new response")
    @PreAuthorize("hasAuthority('ROLE_Parent')")
    public ResponseDto createResponse(@RequestBody ResponseDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update response by Id")
    @PreAuthorize("hasAuthority('ROLE_Parent')")
    public ResponseDto updateResponse(@PathVariable UUID id, @RequestBody ResponseDto dto) {
        return service.update(id, dto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch update response by Id")
    @PreAuthorize("hasAuthority('ROLE_Parent')")
    public ResponseDto patchUpdateResponse(@PathVariable UUID id, @RequestBody ResponseDto dto) {
        return service.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete response by Id")
    @PreAuthorize("hasAuthority('ROLE_Parent')")
    public String deleteResponse(@PathVariable UUID id) {
        return service.delete(id);
    }

}
