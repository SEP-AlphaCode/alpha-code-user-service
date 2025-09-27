package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.ResponseDto;
import com.alpha_code.alpha_code_user_service.entity.Response;
import com.alpha_code.alpha_code_user_service.mapper.ResponseMapper;
import com.alpha_code.alpha_code_user_service.repository.RequestRepository;
import com.alpha_code.alpha_code_user_service.repository.ResponseRepository;
import com.alpha_code.alpha_code_user_service.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResponseServiceImpl implements ResponseService {

    private final ResponseRepository repository;
    private final RequestRepository requestRepository;

    @Override
    @Cacheable(value = "responses_list", key = "{#page, #size, #keyword, #status, #requestId, #responderId}")
    public PagedResult<ResponseDto> getAll(int page, int size, Integer status, UUID requestId, UUID responderId) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Response> pageResult;

        pageResult = repository.search(status, requestId, responderId, pageable);

        return new PagedResult<>(pageResult.map(ResponseMapper::toDto));

    }

    @Override
    @Cacheable(value = "responses", key = "#id")
    public ResponseDto getById(UUID id) {
        var response = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found"));

        return ResponseMapper.toDto(response);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"responses_list", "responses"}, allEntries = true)
    public ResponseDto create(ResponseDto responseDto) {
        var req = requestRepository.findById(responseDto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        var entity = ResponseMapper.toEntity(responseDto);
        entity.setCreatedDated(LocalDateTime.now());

        var savedEntity = repository.save(entity);
        return ResponseMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"responses_list", "responses"}, allEntries = true)
    @CachePut(value = "responses", key = "#id")
    public ResponseDto update(UUID id, ResponseDto responseDto) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found"));

        entity.setRequestId(responseDto.getRequestId());
        entity.setResponderId(responseDto.getResponderId());
        entity.setResponseContent(responseDto.getResponseContent());
        entity.setStatus(responseDto.getStatus());

        entity.setLastUpdated(LocalDateTime.now());

        Response savedEntity = repository.save(entity);
        return ResponseMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"responses_list", "responses"}, allEntries = true)
    @CachePut(value = "responses", key = "#id")
    public ResponseDto patch(UUID id, ResponseDto responseDto) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found"));

        if (responseDto.getRequestId() != null) {
            entity.setRequestId(responseDto.getRequestId());
        }
        if (responseDto.getResponderId() != null) {
            entity.setResponderId(responseDto.getResponderId());
        }
        if (responseDto.getResponseContent() != null) {
            entity.setResponseContent(responseDto.getResponseContent());
        }
        if (responseDto.getStatus() != null) {
            entity.setStatus(responseDto.getStatus());
        }

        entity.setLastUpdated(LocalDateTime.now());

        Response savedEntity = repository.save(entity);
        return ResponseMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"responses_list", "responses"}, allEntries = true)
    public String delete(UUID id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found"));

        entity.setStatus(0);
        entity.setLastUpdated(LocalDateTime.now());

        repository.save(entity);
        return "Response deleted successfully";
    }

    @Override
    @Transactional
    @CacheEvict(value = "responses", allEntries = true)
    @CachePut(value = "responses", key = "#id")
    public ResponseDto changeStatus(UUID id, Integer status) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Response not found"));

        entity.setStatus(status);
        entity.setLastUpdated(LocalDateTime.now());

        Response savedEntity = repository.save(entity);
        return ResponseMapper.toDto(savedEntity);
    }
}
