package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.AccountDto;
import com.alpha_code.alpha_code_user_service.dto.LoginDto;
import com.alpha_code.alpha_code_user_service.dto.ResetPassworDto;
import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.entity.Role;
import com.alpha_code.alpha_code_user_service.exception.AuthenticationException;
import com.alpha_code.alpha_code_user_service.exception.ConflictException;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.mapper.AccountMapper;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.RoleRepository;
import com.alpha_code.alpha_code_user_service.service.AuthService;
import com.alpha_code.alpha_code_user_service.service.DashboardService;
import com.alpha_code.alpha_code_user_service.service.RedisRefreshTokenService;
import com.alpha_code.alpha_code_user_service.service.S3Service;
import com.alpha_code.alpha_code_user_service.util.EmailBody;
import com.alpha_code.alpha_code_user_service.util.JwtUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository repository;
//    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final RedisRefreshTokenService redisRefreshTokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;
    private final DashboardService dashboardService;
    @Value("${web-base-url}")
    private String webBaseUrl;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public LoginDto.LoginResponse login(LoginDto.LoginRequest loginRequest) {
        Optional<Account> accountOptional = repository.findAccountByUsername(loginRequest.getUsername().trim());
        if (accountOptional.isEmpty()) {
            accountOptional = repository.findByEmail(loginRequest.getUsername().trim());
        }

        // If no account is found, throw AuthenticationException
        Account account = accountOptional.orElseThrow(() ->
                new AuthenticationException("Sai tài khoản hoặc mật khẩu"));

        if (!passwordEncoder.matches(loginRequest.getPassword().trim(), account.getPassword())) {
            throw new AuthenticationException("Sai tài khoản hoặc mật khẩu");
        }

        if (account.getStatus() == 0) {
            return LoginDto.LoginResponse.builder()
                    .accessToken(null)
                    .refreshToken(null)
                    .build();
        }

        // Generate JWT token and return response
        String accessToken = jwtUtil.generateAccessToken(account);
        String refreshToken = jwtUtil.generateRefreshToken(account);

        // Lưu refresh token vào Redis
        redisRefreshTokenService.save(
                account.getId(),
                refreshToken,
                refreshTokenExpirationMs,
                TimeUnit.MILLISECONDS
        );

        dashboardService.addOnlineUser(account.getId());

        return LoginDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AccountDto register(LoginDto.RegisterRequest registerRequest) {
        if (repository.existsByUsername(registerRequest.getUsername().trim())) {
            throw new ConflictException("Tên đăng nhập đã được sử dụng");
        }
        if (repository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email đã được sử dụng");
        }
        if (repository.existsByPhone(registerRequest.getPhone())) {
            throw new ConflictException("Số điện thoại đã được sử dụng");
        }

        Account entity = new Account();
        entity.setUsername(registerRequest.getUsername().trim());
        entity.setPassword(passwordEncoder.encode(registerRequest.getPassword().trim()));
        entity.setFullName(registerRequest.getFullName());
        entity.setEmail(registerRequest.getEmail());
        entity.setPhone(registerRequest.getPhone());
        entity.setGender(registerRequest.getGender());
        entity.setStatus(1);
        var roleUser = roleRepository.findByNameIgnoreCase("USER");
        if (roleUser.isEmpty()) {
            Role newRole = new Role();
            newRole.setName("USER");
            newRole.setStatus(1);
            roleRepository.save(newRole);
            roleUser = roleRepository.findByNameIgnoreCase("USER");
        }

        if (registerRequest.getAvatarFile() != null && !registerRequest.getAvatarFile().isEmpty()) {
            try {
                String fileKey = "avatars/" + System.currentTimeMillis() + "_" + registerRequest.getAvatarFile().getOriginalFilename();
                String avatarUrl = s3Service.uploadBytes(registerRequest.getAvatarFile().getBytes(), fileKey, registerRequest.getAvatarFile().getContentType());
                entity.setImage(avatarUrl);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi tải Ảnh đại diện", e);
            }

        } else {
            try {
                ClassPathResource resource = new ClassPathResource("images/alphacode-logo.png");
                byte[] defaultImageBytes = resource.getInputStream().readAllBytes();

                // Encode sang base64
                String base64Image = Base64.getEncoder().encodeToString(defaultImageBytes);

                // Thêm prefix để FE load trực tiếp
                String dataUri = "data:image/png;base64," + base64Image;

                entity.setImage(dataUri);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi đọc ảnh mặc định", e);
            }
        }
        entity.setRoleId(roleUser.get().getId());
        entity.setRole(roleUser.get());
        entity.setCreatedDate(LocalDateTime.now());

        Account savedEntity = repository.save(entity);

        return AccountMapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    public LoginDto.LoginResponse googleLogin(String request) {
        try {
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(request);

            String email = firebaseToken.getEmail();
            String name = (String) firebaseToken.getClaims().get("name");
//            String sub = payload.getSubject();

            Account account = repository.findByEmail(email).orElse(null);

            // If no account is found, create a new account
            if (account == null) {
                Account entity = new Account();
                entity.setUsername(email);
                entity.setPassword(passwordEncoder.encode(""));
                entity.setFullName(name);
                entity.setEmail(email);
                entity.setPhone("");
                entity.setGender(0);
                entity.setImage(firebaseToken.getPicture());
                entity.setCreatedDate(LocalDateTime.now());
                entity.setStatus(1);
                var roleUser = roleRepository.findByNameIgnoreCase("USER");
                if (roleUser.isEmpty()) {
                    Role newRole = new Role();
                    newRole.setName("USER");
                    newRole.setStatus(1);
                    roleRepository.save(newRole);
                    roleUser = roleRepository.findByNameIgnoreCase("USER");
                }
                entity.setRoleId(roleUser.get().getId());
                entity.setRole(roleUser.get());
                Account savedEntity = repository.save(entity);
                account = savedEntity;
            }

            String accessToken = jwtUtil.generateAccessToken(account);
            String refreshToken = jwtUtil.generateRefreshToken(account);

            // Lưu refresh token vào Redis
            redisRefreshTokenService.save(
                    account.getId(),
                    refreshToken,
                    refreshTokenExpirationMs,
                    TimeUnit.MILLISECONDS
            );

            dashboardService.addOnlineUser(account.getId());

            return LoginDto.LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (Exception e) {
            throw new AuthenticationException("Google login failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean requestResetPassword(String email) throws MessagingException {
        var account = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found!"));

        System.out.println("Account get = " + account.getId());

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Đặt lại mật khẩu - AlphaCode");

        var resetToken = jwtUtil.generateResetPasswordToken(account);
        var resetLink = webBaseUrl + "/reset-password/reset?token=" + resetToken;

        var emailBody = EmailBody.getResetPasswordEmailBody(account.getFullName(), resetLink);

        helper.setText(emailBody, true);

        // Put logo picture (inline image với cid:alphacode-logo)
        ClassPathResource logoImage = new ClassPathResource("images/alphacode-logo.png");
        helper.addInline("alphacode-logo", logoImage);

        mailSender.send(message);
        return true;
    }

    @Override
    @Transactional
    public boolean confirmResetPassword(ResetPassworDto dto) {
        String email = jwtUtil.extractEmail(dto.getResetToken());

        Account account = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));

        // 2. Check token is valid or not
        if (!jwtUtil.validateJwtToken(dto.getResetToken())) {
            throw new IllegalArgumentException("Reset token is invalid or expired");
        }

        // 3. Hash new password
        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        // 4. Save new password to db
        repository.save(account);
        return true;
    }
}
