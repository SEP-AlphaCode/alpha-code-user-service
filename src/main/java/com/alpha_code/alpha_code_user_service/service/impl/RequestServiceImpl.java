package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.RequestDto;
import com.alpha_code.alpha_code_user_service.entity.Request;
import com.alpha_code.alpha_code_user_service.mapper.RequestMapper;
import com.alpha_code.alpha_code_user_service.repository.RequestRepository;
import com.alpha_code.alpha_code_user_service.service.RequestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository repository;

    @Override
    @Cacheable(value = "Requests_list", key = "{#page, #size, #title, #type, #status, #accountId, #rate}")
    public PagedResult<RequestDto> getAllRequest(int page, int size, String title, String type, Integer status, UUID accountId, Integer rate) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Request> pageResult;

        pageResult = repository.searchRequests(title, type, status, accountId, rate, pageable);
        return new PagedResult<>(pageResult.map(RequestMapper::toDto));
    }

    @Override
    @Cacheable(value = "Requests_id", key = "#id")
    public RequestDto getRequestById(UUID id) {
        var Request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User request not found"));
        return RequestMapper.toDto(Request);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"Requests_list", "Requests_id"}, allEntries = true)
    public RequestDto createRequest(RequestDto RequestDto) {
        var Request = RequestMapper.toEntity(RequestDto);

        Request.setCreatedDated(LocalDateTime.now());

        Request savedRequest = repository.save(Request);
        return RequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    @CachePut(value = "Requests_id", key = "#RequestDto.id")
    public RequestDto updateRequest(UUID id, RequestDto RequestDto) {
        var Request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User request not found"));

        Request.setAccountId(RequestDto.getAccountId());
        Request.setTitle(RequestDto.getTitle());
        Request.setDescription(RequestDto.getDescription());
        Request.setType(RequestDto.getType());
        Request.setRate(RequestDto.getRate());
        Request.setStatus(RequestDto.getStatus());
        Request.setLastUpdated(LocalDateTime.now());

        Request savedRequest = repository.save(Request);
        return RequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    @CachePut(value = "Requests_id", key = "#RequestDto.id")
    public RequestDto patchRequest(UUID id, RequestDto RequestDto) {
        var Request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User request not found"));

        if (RequestDto.getAccountId() != null) {
            Request.setAccountId(RequestDto.getAccountId());
        }
        if (RequestDto.getTitle() != null) {
            Request.setTitle(RequestDto.getTitle());
        }
        if (RequestDto.getDescription() != null) {
            Request.setDescription(RequestDto.getDescription());
        }
        if (RequestDto.getType() != null) {
            Request.setType(RequestDto.getType());
        }
        if (RequestDto.getRate() != null) {
            Request.setRate(RequestDto.getRate());
        }
        if (RequestDto.getStatus() != null) {
            Request.setStatus(RequestDto.getStatus());
        }
        Request.setLastUpdated(LocalDateTime.now());

        Request savedRequest = repository.save(Request);
        return RequestMapper.toDto(savedRequest);
    }

    @Override
    @Transactional
    @CacheEvict(value = "Requests_id", key = "#id")
    public String deleteRequest(UUID id) {
        var Request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User request not found"));

        Request.setStatus(0);
        Request.setLastUpdated(LocalDateTime.now());

        repository.save(Request);
        return "Xóa thành công User request với ID: " + id;
    }

    @Override
    @Transactional
    @CacheEvict(value = "Requests_id", key = "#id")
    @CachePut(value = "Requests_id", key = "#id")
    public RequestDto changeStatus(UUID id, Integer status) {

        var Request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User request not found"));

        Request.setStatus(status);
        Request.setLastUpdated(LocalDateTime.now());

        Request savedRequest = repository.save(Request);
        return RequestMapper.toDto(savedRequest);
    }
}
