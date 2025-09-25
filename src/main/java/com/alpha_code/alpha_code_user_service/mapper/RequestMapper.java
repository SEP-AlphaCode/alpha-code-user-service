package com.alpha_code.alpha_code_user_service.mapper;

import com.alpha_code.alpha_code_user_service.dto.RequestDto;
import com.alpha_code.alpha_code_user_service.entity.Request;

public class RequestMapper {
    public static RequestDto toDto(Request Request) {
        if (Request == null) {
            return null;
        }

        RequestDto dto = new RequestDto();
        dto.setId(Request.getId());
        dto.setAccountId(Request.getAccountId());
        if (Request.getAccount() != null) {
            dto.setAccountFullName(Request.getAccount().getFullName());
        }
        dto.setTitle(Request.getTitle());
        dto.setDescription(Request.getDescription());
        dto.setType(Request.getType());
        dto.setRate(Request.getRate());
        dto.setStatus(Request.getStatus());
        dto.setCreatedDated(Request.getCreatedDated());
        dto.setLastUpdated(Request.getLastUpdated());
        return dto;
    }

    public static Request toEntity(RequestDto dto) {
        if (dto == null) {
            return null;
        }

        Request Request = new Request();
        Request.setId(dto.getId());
        Request.setAccountId(dto.getAccountId());
        Request.setTitle(dto.getTitle());
        Request.setDescription(dto.getDescription());
        Request.setType(dto.getType());
        Request.setRate(dto.getRate());
        Request.setStatus(dto.getStatus());
        Request.setCreatedDated(dto.getCreatedDated());
        Request.setLastUpdated(dto.getLastUpdated());
        return Request;
    }
}
