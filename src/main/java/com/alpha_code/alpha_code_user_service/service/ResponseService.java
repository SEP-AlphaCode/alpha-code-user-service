package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.ResponseDto;

import java.util.UUID;

public interface ResponseService {
    PagedResult<ResponseDto> getAll(int page, int size, String keyword, Integer status, UUID requestId, UUID responderId);

    ResponseDto getById(UUID id);

    ResponseDto create(ResponseDto responseDto);

    ResponseDto update(UUID id, ResponseDto responseDto);

    ResponseDto patch(UUID id, ResponseDto responseDto);

    String delete(UUID id);

    ResponseDto changeStatus(UUID id, Integer status);
}
