package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.RequestDto;
import com.alpha_code.alpha_code_user_service.entity.Request;

import java.util.UUID;

public interface RequestService {
    PagedResult<RequestDto> getAllRequest(int page, int size, String title, String type, Integer status, UUID accountId, Integer rate);

    RequestDto getRequestById(UUID id);

    RequestDto createRequest(RequestDto RequestDto);

    RequestDto updateRequest(UUID id, RequestDto RequestDto);

    RequestDto patchRequest(UUID id, RequestDto RequestDto);

    String deleteRequest(UUID id);

    RequestDto changeStatus(UUID id, Integer status);
}
