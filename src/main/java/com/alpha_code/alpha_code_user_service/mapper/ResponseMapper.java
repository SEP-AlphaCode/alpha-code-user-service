package com.alpha_code.alpha_code_user_service.mapper;

import com.alpha_code.alpha_code_user_service.dto.ResponseDto;
import com.alpha_code.alpha_code_user_service.entity.Response;

public class ResponseMapper {
    public static ResponseDto toDto(Response response) {
        if (response == null) {
            return null;
        }

        ResponseDto dto = new ResponseDto();
        dto.setId(response.getId());
        dto.setResponderId(response.getResponderId());
        if (response.getResponder() != null) {
            dto.setResponderFullname(response.getResponder().getFullName());
        }
        dto.setRequestId(response.getRequestId());
        if (response.getRequest() != null) {
            dto.setRequestTitle(response.getRequest().getTitle());
        }
        dto.setResponseContent(response.getResponseContent());
        dto.setStatus(response.getStatus());
        dto.setCreatedDated(response.getCreatedDated());
        dto.setLastUpdated(response.getLastUpdated());
        return dto;
    }

    public static Response toEntity(ResponseDto dto) {
        if (dto == null) return null;

        Response response = new Response();
        response.setId(dto.getId());
        response.setResponderId(dto.getResponderId());
        response.setRequestId(dto.getRequestId());
        response.setResponseContent(dto.getResponseContent());
        response.setStatus(dto.getStatus());
        response.setCreatedDated(dto.getCreatedDated());
        response.setLastUpdated(dto.getLastUpdated());
        return response;
    }
}
