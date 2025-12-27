package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.RequestDto;
import com.alpha_code.alpha_code_user_service.entity.Request;
import com.alpha_code.alpha_code_user_service.repository.RequestRepository;
import com.alpha_code.alpha_code_user_service.service.impl.RequestServiceImpl;
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
class RequestServiceTest {

    @Mock
    private RequestRepository repository;

    @InjectMocks
    private RequestServiceImpl requestService;

    private Request request;
    private RequestDto requestDto;
    private UUID requestId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        request = new Request();
        request.setId(requestId);
        request.setAccountId(accountId);
        request.setTitle("Test Request");
        request.setDescription("Test Description");
        request.setType("FEATURE");
        request.setStatus(1);
        request.setRate(5);
        request.setCreatedDated(LocalDateTime.now());

        requestDto = new RequestDto();
        requestDto.setId(requestId);
        requestDto.setAccountId(accountId);
        requestDto.setTitle("Test Request");
        requestDto.setDescription("Test Description");
        requestDto.setType("FEATURE");
        requestDto.setStatus(1);
        requestDto.setRate(5);
    }

    @Test
    void testGetAllRequest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Request> page = new PageImpl<>(java.util.List.of(request), pageable, 1);

        when(repository.searchRequests(null, null, null, null, null, pageable)).thenReturn(page);

        var result = requestService.getAllRequest(1, 10, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        verify(repository).searchRequests(null, null, null, null, null, pageable);
    }

    @Test
    void testGetRequestById_Success() {
        when(repository.findById(requestId)).thenReturn(java.util.Optional.of(request));

        var result = requestService.getRequestById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(repository).findById(requestId);
    }

    @Test
    void testGetRequestById_NotFound() {
        when(repository.findById(requestId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> requestService.getRequestById(requestId));
    }

    @Test
    void testCreateRequest_Success() {
        when(repository.save(any(Request.class))).thenReturn(request);

        var result = requestService.createRequest(requestDto);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(repository).save(any(Request.class));
    }

    @Test
    void testUpdateRequest_Success() {
        when(repository.findById(requestId)).thenReturn(java.util.Optional.of(request));
        when(repository.save(any(Request.class))).thenReturn(request);

        var result = requestService.updateRequest(requestId, requestDto);

        assertNotNull(result);
        verify(repository).findById(requestId);
        verify(repository).save(any(Request.class));
    }

    @Test
    void testUpdateRequest_NotFound() {
        when(repository.findById(requestId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> requestService.updateRequest(requestId, requestDto));
    }

    @Test
    void testPatchRequest_Success() {
        when(repository.findById(requestId)).thenReturn(java.util.Optional.of(request));
        when(repository.save(any(Request.class))).thenReturn(request);

        var result = requestService.patchRequest(requestId, requestDto);

        assertNotNull(result);
        verify(repository).findById(requestId);
        verify(repository).save(any(Request.class));
    }

    @Test
    void testDeleteRequest_Success() {
        when(repository.findById(requestId)).thenReturn(java.util.Optional.of(request));
        when(repository.save(any(Request.class))).thenReturn(request);

        var result = requestService.deleteRequest(requestId);

        assertNotNull(result);
        assertTrue(result.contains("Xóa thành công"));
        verify(repository).findById(requestId);
        verify(repository).save(any(Request.class));
    }

    @Test
    void testChangeStatus_Success() {
        when(repository.findById(requestId)).thenReturn(java.util.Optional.of(request));
        when(repository.save(any(Request.class))).thenReturn(request);

        var result = requestService.changeStatus(requestId, 2);

        assertNotNull(result);
        assertEquals(2, result.getStatus());
        verify(repository).findById(requestId);
        verify(repository).save(any(Request.class));
    }
}

