package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.AccountDto;
import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.entity.Role;
import com.alpha_code.alpha_code_user_service.exception.AuthenticationException;
import com.alpha_code.alpha_code_user_service.exception.ConflictException;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.RoleRepository;
import com.alpha_code.alpha_code_user_service.service.impl.AccountServiceImpl;
import com.alpha_code.alpha_code_user_service.service.S3Service;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository repository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;
    private AccountDto accountDto;
    private Role role;
    private UUID accountId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        role = new Role();
        role.setId(roleId);
        role.setName("USER");
        role.setStatus(1);

        account = new Account();
        account.setId(accountId);
        account.setUsername("testuser");
        account.setPassword("encodedPassword");
        account.setFullName("Test User");
        account.setEmail("test@example.com");
        account.setPhone("1234567890");
        account.setGender(1);
        account.setImage("image.jpg");
        account.setStatus(1);
        account.setRoleId(roleId);
        account.setRole(role);
        account.setCreatedDate(LocalDateTime.now());

        accountDto = new AccountDto();
        accountDto.setId(accountId);
        accountDto.setUsername("testuser");
        accountDto.setPassword("password123");
        accountDto.setFullName("Test User");
        accountDto.setEmail("test@example.com");
        accountDto.setPhone("1234567890");
        accountDto.setGender(1);
        accountDto.setRoleId(roleId);
        accountDto.setStatus(1);
    }

    @Test
    void testGetAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(java.util.List.of(account), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);

        var result = accountService.getAll(1, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        verify(repository).findAll(pageable);
    }

    @Test
    void testGetAllWithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(java.util.List.of(account), pageable, 1);

        when(repository.findAllByStatus(1, pageable)).thenReturn(page);

        var result = accountService.getAll(1, 10, 1);

        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        verify(repository).findAllByStatus(1, pageable);
    }

    @Test
    void testGetById_Success() {
        when(repository.findById(accountId)).thenReturn(Optional.of(account));

        var result = accountService.getById(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.getId());
        verify(repository).findById(accountId);
    }

    @Test
    void testGetById_NotFound() {
        when(repository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getById(accountId));
        verify(repository).findById(accountId);
    }

    @Test
    void testCreate_Success() throws Exception {
        when(repository.existsByUsername(accountDto.getUsername())).thenReturn(false);
        when(repository.existsByEmail(accountDto.getEmail())).thenReturn(false);
        when(repository.existsByPhone(accountDto.getPhone())).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(repository.save(any(Account.class))).thenReturn(account);

        var result = accountService.create(accountDto, null);

        assertNotNull(result);
        verify(repository).existsByUsername(accountDto.getUsername());
        verify(repository).existsByEmail(accountDto.getEmail());
        verify(repository).existsByPhone(accountDto.getPhone());
        verify(repository).save(any(Account.class));
    }

    @Test
    void testCreate_UsernameExists() {
        when(repository.existsByUsername(accountDto.getUsername())).thenReturn(true);

        assertThrows(ConflictException.class, () -> accountService.create(accountDto, null));
        verify(repository).existsByUsername(accountDto.getUsername());
        verify(repository, never()).save(any(Account.class));
    }

    @Test
    void testCreate_EmailExists() {
        when(repository.existsByUsername(accountDto.getUsername())).thenReturn(false);
        when(repository.existsByEmail(accountDto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> accountService.create(accountDto, null));
        verify(repository).existsByEmail(accountDto.getEmail());
    }

    @Test
    void testUpdate_Success() {
        when(repository.findById(accountId)).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenReturn(account);

        var result = accountService.update(accountId, accountDto);

        assertNotNull(result);
        verify(repository).findById(accountId);
        verify(repository).save(any(Account.class));
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.update(accountId, accountDto));
        verify(repository).findById(accountId);
        verify(repository, never()).save(any(Account.class));
    }

    @Test
    void testChangeStatus_Success() {
        Role userRole = new Role();
        userRole.setId(roleId);
        userRole.setName("USER");
        account.setRole(userRole);

        when(repository.findAccountById(accountId)).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenReturn(account);

        var result = accountService.changeStatus(accountId, 0, "Banned");

        assertNotNull(result);
        verify(repository).findAccountById(accountId);
        verify(repository).save(any(Account.class));
    }

    @Test
    void testChangeStatus_AdminCannotBeChanged() {
        role.setName("Admin");
        account.setRole(role);

        when(repository.findAccountById(accountId)).thenReturn(Optional.of(account));

        assertThrows(ConflictException.class, 
            () -> accountService.changeStatus(accountId, 0, "Banned"));
        verify(repository).findAccountById(accountId);
        verify(repository, never()).save(any(Account.class));
    }

    @Test
    void testDelete_Success() {
        when(repository.findById(accountId)).thenReturn(Optional.of(account));
        when(repository.save(any(Account.class))).thenReturn(account);

        var result = accountService.delete(accountId);

        assertNotNull(result);
        verify(repository).findById(accountId);
        verify(repository).save(any(Account.class));
    }

    @Test
    void testDelete_NotFound() {
        when(repository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountService.delete(accountId));
        verify(repository).findById(accountId);
    }

    @Test
    void testFindAccountByFullName() {
        when(repository.findAccountByFullName("Test User")).thenReturn(account);

        var result = accountService.findAccountByFullName("Test User");

        assertNotNull(result);
        verify(repository).findAccountByFullName("Test User");
    }

    @Test
    void testFindAccountByIdGrpc_Success() {
        when(repository.findAccountByIdGrpc(accountId)).thenReturn(Optional.of(account));

        var result = accountService.findAccountByIdGrpc(accountId);

        assertNotNull(result);
        verify(repository).findAccountByIdGrpc(accountId);
    }

    @Test
    void testFindAccountByIdGrpc_NotFound() {
        when(repository.findAccountByIdGrpc(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.findAccountByIdGrpc(accountId));
        verify(repository).findAccountByIdGrpc(accountId);
    }
}

