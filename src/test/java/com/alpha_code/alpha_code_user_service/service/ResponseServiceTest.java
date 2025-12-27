package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.ResponseDto;
import com.alpha_code.alpha_code_user_service.entity.Request;
import com.alpha_code.alpha_code_user_service.entity.Response;
import com.alpha_code.alpha_code_user_service.repository.RequestRepository;
import com.alpha_code.alpha_code_user_service.repository.ResponseRepository;
import com.alpha_code.alpha_code_user_service.service.impl.ResponseServiceImpl;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResponseServiceTest {

    @Mock
    private ResponseRepository repository;

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private ResponseServiceImpl responseService;

    private Response response;
    private ResponseDto responseDto;
    private Request request;
    private UUID responseId;
    private UUID requestId;
    private UUID responderId;

    @BeforeEach
    void setUp() {
        responseId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        responderId = UUID.randomUUID();

        request = new Request();
        request.setId(requestId);
        request.setTitle("Test Request");

        response = new Response();
        response.setId(responseId);
        response.setRequestId(requestId);
        response.setResponderId(responderId);
        response.setResponseContent("Test Response");
        response.setStatus(1);
        response.setCreatedDated(LocalDateTime.now());

        responseDto = new ResponseDto();
        responseDto.setId(responseId);
        responseDto.setRequestId(requestId);
        responseDto.setResponderId(responderId);
        responseDto.setResponseContent("Test Response");
        responseDto.setStatus(1);
    }

    @Test
    void testGetAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Response> page = new PageImpl<>(java.util.List.of(response), pageable, 1);

        when(repository.search(null, null, null, pageable)).thenReturn(page);

        var result = responseService.getAll(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        verify(repository).search(null, null, null, pageable);
    }

    @Test
    void testGetById_Success() {
        when(repository.findById(responseId)).thenReturn(java.util.Optional.of(response));

        var result = responseService.getById(responseId);

        assertNotNull(result);
        assertEquals(responseId, result.getId());
        verify(repository).findById(responseId);
    }

    @Test
    void testGetById_NotFound() {
        when(repository.findById(responseId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> responseService.getById(responseId));
    }

    @Test
    void testCreate_Success() {
        when(requestRepository.findById(requestId)).thenReturn(java.util.Optional.of(request));
        when(repository.save(any(Response.class))).thenReturn(response);

        var result = responseService.create(responseDto);

        assertNotNull(result);
        assertEquals(responseId, result.getId());
        verify(requestRepository).findById(requestId);
        verify(repository).save(any(Response.class));
    }

    @Test
    void testCreate_RequestNotFound() {
        when(requestRepository.findById(requestId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> responseService.create(responseDto));
    }

    @Test
    void testUpdate_Success() {
        when(repository.findById(responseId)).thenReturn(java.util.Optional.of(response));
        when(repository.save(any(Response.class))).thenReturn(response);

        var result = responseService.update(responseId, responseDto);

        assertNotNull(result);
        verify(repository).findById(responseId);
        verify(repository).save(any(Response.class));
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.findById(responseId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> responseService.update(responseId, responseDto));
    }

    @Test
    void testPatch_Success() {
        when(repository.findById(responseId)).thenReturn(java.util.Optional.of(response));
        when(repository.save(any(Response.class))).thenReturn(response);

        var result = responseService.patch(responseId, responseDto);

        assertNotNull(result);
        verify(repository).findById(responseId);
        verify(repository).save(any(Response.class));
    }

    @Test
    void testDelete_Success() {
        when(repository.findById(responseId)).thenReturn(java.util.Optional.of(response));
        when(repository.save(any(Response.class))).thenReturn(response);

        var result = responseService.delete(responseId);

        assertNotNull(result);
        assertTrue(result.contains("deleted successfully"));
        verify(repository).findById(responseId);
        verify(repository).save(any(Response.class));
    }

    @Test
    void testChangeStatus_Success() {
        when(repository.findById(responseId)).thenReturn(java.util.Optional.of(response));
        when(repository.save(any(Response.class))).thenReturn(response);

        var result = responseService.changeStatus(responseId, 2);

        assertNotNull(result);
        assertEquals(2, result.getStatus());
        verify(repository).findById(responseId);
        verify(repository).save(any(Response.class));
    }
}

