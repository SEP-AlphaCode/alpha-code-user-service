package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.RoleDto;
import com.alpha_code.alpha_code_user_service.entity.Role;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.mapper.RoleMapper;
import com.alpha_code.alpha_code_user_service.repository.RoleRepository;
import com.alpha_code.alpha_code_user_service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;

    @Override
    @Cacheable(value = "roles_list", key = "{#page, #size, #status}")
    public PagedResult<RoleDto> getAll(int page, int size, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Role> pageResult;

        if (status != null) {
            pageResult = repository.findAllByStatus(status, pageable);
        } else {
            pageResult = repository.findAll(pageable);
        }

        return new PagedResult<>(pageResult.map(RoleMapper::toDto));
    }

    @Override
    @Cacheable(value = "roles", key = "#id")
    public RoleDto getById(UUID id) {
        var role = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return RoleMapper.toDto(role);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"roles_list", "roles"}, allEntries = true)
    public RoleDto create(RoleDto roleDto) {
        if (repository.findByName(roleDto.getName()) != null) {
            throw new IllegalArgumentException("Role with this name already exists");
        }

        var role = RoleMapper.toEntity(roleDto);
        role.setStatus(1);

        var savedRole = repository.save(role);
        return RoleMapper.toDto(savedRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"roles_list"}, allEntries = true)
    @CachePut(value = "roles", key = "#id")
    public RoleDto update(UUID id, RoleDto roleDto) {
        var existingRole = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (repository.findByName(roleDto.getName()) != null &&
                !existingRole.getName().equals(roleDto.getName())) {
            throw new IllegalArgumentException("Role with this name already exists");
        }

        existingRole.setName(roleDto.getName());

        var updatedRole = repository.save(existingRole);
        return RoleMapper.toDto(updatedRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"roles_list"}, allEntries = true)
    @CachePut(value = "roles", key = "#id")
    public RoleDto patchUpdate(UUID id, RoleDto roleDto) {
        var existingRole = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (repository.findByName(roleDto.getName()) != null &&
                !existingRole.getName().equals(roleDto.getName())) {
            throw new IllegalArgumentException("Role with this name already exists");
        }

        if (roleDto.getName() != null)
            existingRole.setName(roleDto.getName());

        var updatedRole = repository.save(existingRole);
        return RoleMapper.toDto(updatedRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"roles_list", "roles"}, allEntries = true)
    public String delete(UUID id) {
        try {
            var role = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

            if (role.getAccounts() != null && !role.getAccounts().isEmpty()) {
                throw new IllegalArgumentException("Cannot delete role with associated accounts");
            }

//        repository.delete(role);
            role.setStatus(0);
            repository.save(role);
            return "Role deleted successfully with ID: " + id;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting role", e);
        }

    }

    @Override
    @Transactional
    @CacheEvict(value = {"roles_list"}, allEntries = true)
    @CachePut(value = "roles", key = "#id")
    public RoleDto changeStatus(UUID id, Integer status) {
        Role entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        entity.setStatus(status);

        Role updated = repository.save(entity);
        return RoleMapper.toDto(updated);
    }
}
