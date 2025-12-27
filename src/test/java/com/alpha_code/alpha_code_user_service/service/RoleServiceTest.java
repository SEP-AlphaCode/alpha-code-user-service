package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.dto.RoleDto;
import com.alpha_code.alpha_code_user_service.entity.Role;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.repository.RoleRepository;
import com.alpha_code.alpha_code_user_service.service.impl.RoleServiceImpl;
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

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository repository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role role;
    private RoleDto roleDto;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();

        role = new Role();
        role.setId(roleId);
        role.setName("USER");
        role.setStatus(1);

        roleDto = new RoleDto();
        roleDto.setId(roleId);
        roleDto.setName("USER");
        roleDto.setStatus(1);
    }

    @Test
    void testGetAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> page = new PageImpl<>(java.util.List.of(role), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);

        var result = roleService.getAll(1, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        verify(repository).findAll(pageable);
    }

    @Test
    void testGetAllWithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> page = new PageImpl<>(java.util.List.of(role), pageable, 1);

        when(repository.findAllByStatus(1, pageable)).thenReturn(page);

        var result = roleService.getAll(1, 10, 1);

        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        verify(repository).findAllByStatus(1, pageable);
    }

    @Test
    void testGetById_Success() {
        when(repository.findById(roleId)).thenReturn(java.util.Optional.of(role));

        var result = roleService.getById(roleId);

        assertNotNull(result);
        assertEquals(roleId, result.getId());
        verify(repository).findById(roleId);
    }

    @Test
    void testGetById_NotFound() {
        when(repository.findById(roleId)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.getById(roleId));
    }

    @Test
    void testCreate_Success() {
        when(repository.findByName("USER")).thenReturn(null);
        when(repository.save(any(Role.class))).thenReturn(role);

        var result = roleService.create(roleDto);

        assertNotNull(result);
        verify(repository).findByName("USER");
        verify(repository).save(any(Role.class));
    }

    @Test
    void testCreate_NameExists() {
        when(repository.findByName("USER")).thenReturn(role);

        assertThrows(IllegalArgumentException.class, () -> roleService.create(roleDto));
    }

    @Test
    void testUpdate_Success() {
        when(repository.findById(roleId)).thenReturn(java.util.Optional.of(role));
        when(repository.findByName("ADMIN")).thenReturn(null);
        when(repository.save(any(Role.class))).thenReturn(role);

        roleDto.setName("ADMIN");
        var result = roleService.update(roleId, roleDto);

        assertNotNull(result);
        verify(repository).findById(roleId);
        verify(repository).save(any(Role.class));
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.findById(roleId)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.update(roleId, roleDto));
    }

    @Test
    void testPatchUpdate_Success() {
        when(repository.findById(roleId)).thenReturn(java.util.Optional.of(role));
        when(repository.findByName("ADMIN")).thenReturn(null);
        when(repository.save(any(Role.class))).thenReturn(role);

        roleDto.setName("ADMIN");
        var result = roleService.patchUpdate(roleId, roleDto);

        assertNotNull(result);
        verify(repository).findById(roleId);
        verify(repository).save(any(Role.class));
    }

    @Test
    void testDelete_Success() {
        role.setAccounts(new ArrayList<>());
        when(repository.findById(roleId)).thenReturn(java.util.Optional.of(role));
        when(repository.save(any(Role.class))).thenReturn(role);

        var result = roleService.delete(roleId);

        assertNotNull(result);
        assertTrue(result.contains("deleted successfully"));
        verify(repository).findById(roleId);
        verify(repository).save(any(Role.class));
    }

    @Test
    void testDelete_WithAssociatedAccounts() {
        role.setAccounts(java.util.List.of(new com.alpha_code.alpha_code_user_service.entity.Account()));
        when(repository.findById(roleId)).thenReturn(java.util.Optional.of(role));

        assertThrows(RuntimeException.class, () -> roleService.delete(roleId));
    }

    @Test
    void testChangeStatus_Success() {
        when(repository.findById(roleId)).thenReturn(java.util.Optional.of(role));
        when(repository.save(any(Role.class))).thenReturn(role);

        var result = roleService.changeStatus(roleId, 0);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        verify(repository).findById(roleId);
        verify(repository).save(any(Role.class));
    }
}

