package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.dto.LoginDto;
import com.alpha_code.alpha_code_user_service.dto.ResetPassworDto;
import com.alpha_code.alpha_code_user_service.dto.request.SwitchProfileRequest;
import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.entity.Profile;
import com.alpha_code.alpha_code_user_service.entity.Role;
import com.alpha_code.alpha_code_user_service.exception.AuthenticationException;
import com.alpha_code.alpha_code_user_service.exception.ConflictException;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.grpc.client.PaymentServiceClient;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.ProfileRepository;
import com.alpha_code.alpha_code_user_service.repository.RoleRepository;
import com.alpha_code.alpha_code_user_service.service.impl.AuthServiceImpl;
import com.alpha_code.alpha_code_user_service.util.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private DashboardService dashboardService;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthServiceImpl authService;

    private Account account;
    private Role role;
    private Profile profile;
    private UUID accountId;
    private UUID roleId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        profileId = UUID.randomUUID();

        role = new Role();
        role.setId(roleId);
        role.setName("USER");
        role.setStatus(1);

        account = new Account();
        account.setId(accountId);
        account.setUsername("testuser");
        account.setPassword(passwordEncoder.encode("password123")); // ✅ encode thật
        account.setEmail("test@example.com");
        account.setStatus(1);
        account.setRole(role);
        account.setRoleId(roleId);
        account.setCreatedDate(LocalDateTime.now());

        profile = new Profile();
        profile.setId(profileId);
        profile.setAccountId(accountId);
        profile.setName("Kid Profile");
        profile.setIsKid(true);
        profile.setStatus(1);
        profile.setRoleId(roleId);
    }

    // ================= LOGIN =================

    @Test
    void login_success_user_has_profile() {
        role.setName("USER");

        LoginDto.LoginRequest request = new LoginDto.LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(accountRepository.findAccountByUsername("testuser"))
                .thenReturn(Optional.of(account));
        when(profileRepository.findByAccountId(accountId))
                .thenReturn(List.of(profile));

        var response = authService.login(request);

        assertTrue(response.getRequiresProfile());
        assertEquals(1, response.getProfiles().size());
        verify(dashboardService, never()).addOnlineUser(any());
    }

    @Test
    void login_wrong_password() {
        LoginDto.LoginRequest request = new LoginDto.LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrong");

        when(accountRepository.findAccountByUsername("testuser"))
                .thenReturn(Optional.of(account));

        assertThrows(AuthenticationException.class,
                () -> authService.login(request));
    }

    // ================= REGISTER =================

    @Test
    void register_success() {
        LoginDto.RegisterRequest request = new LoginDto.RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("123456");
        request.setEmail("new@mail.com");
        request.setFullName("New User");

        when(accountRepository.existsByUsername("newuser")).thenReturn(false);
        when(accountRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(roleRepository.findByNameIgnoreCase("User"))
                .thenReturn(Optional.of(role));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = authService.register(request);

        assertEquals("newuser", result.getUsername());
        assertTrue(passwordEncoder.matches("123456", result.getPassword()));
    }

    @Test
    void register_username_exists() {
        LoginDto.RegisterRequest request = new LoginDto.RegisterRequest();
        request.setUsername("testuser");

        when(accountRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> authService.register(request));
    }

    // ================= RESET PASSWORD =================

    @Test
    void confirm_reset_password_success() {
        ResetPassworDto dto = new ResetPassworDto();
        dto.setResetToken("token");
        dto.setNewPassword("newPass");

        when(jwtUtil.extractEmail("token")).thenReturn("test@example.com");
        when(jwtUtil.validateJwtToken("token")).thenReturn(true);
        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        boolean result = authService.confirmResetPassword(dto);

        assertTrue(result);
        assertTrue(passwordEncoder.matches("newPass", account.getPassword()));
    }
}
