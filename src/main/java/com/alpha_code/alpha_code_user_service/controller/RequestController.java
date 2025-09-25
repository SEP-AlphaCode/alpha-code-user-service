package com.alpha_code.alpha_code_user_service.controller;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.RequestDto;
import com.alpha_code.alpha_code_user_service.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
@Tag(name = "Requests")
@Validated
public class RequestController {

    private final RequestService service;

    @GetMapping
    @Operation(summary = "Get all requests with pagination and optional filters")
    public PagedResult<RequestDto> getAllRequest(@RequestParam(value = "page", defaultValue = "1") int page,
                                                         @RequestParam(value = "size", defaultValue = "10") int size,
                                                         @RequestParam(value = "title", required = false) String title,
                                                         @RequestParam(value = "type", required = false) String type,
                                                         @RequestParam(value = "status", required = false) Integer status,
                                                         @RequestParam(value = "accountId", required = false) UUID accountId,
                                                         @RequestParam(value = "rate", required = false) Integer rate) {
        return service.getAllRequest(page, size, title, type, status, accountId, rate);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get request by id")
    public RequestDto getRequestById(@PathVariable UUID id) {
        return service.getRequestById(id);
    }

    @PostMapping
    @Operation(summary = "Create new request")
    public RequestDto createRequest(@RequestBody RequestDto RequestDto) {
        return service.createRequest(RequestDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update request by id")
    public RequestDto updateRequest(@PathVariable UUID id, @RequestBody RequestDto RequestDto) {
        return service.updateRequest(id, RequestDto);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Patch update request by id")
    public RequestDto patchUpdateRequest(@PathVariable UUID id, @RequestBody RequestDto RequestDto) {
        return service.patchRequest(id, RequestDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete request by id")
    public String deleteRequest(@PathVariable UUID id) {
        return service.deleteRequest(id);
    }

    @PatchMapping("/{id}/change-status")
    @Operation(summary = "Change request status")
    public RequestDto changeStatus(@PathVariable UUID id, @RequestParam Integer status) {
        return service.changeStatus(id, status);
    }
}
