package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.AccountDto;
import com.alpha_code.alpha_code_user_service.dto.LoginDto;
import com.alpha_code.alpha_code_user_service.dto.ProfileDto;
import com.alpha_code.alpha_code_user_service.dto.ResetPassworDto;
import com.alpha_code.alpha_code_user_service.dto.request.SwitchProfileRequest;
import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.entity.Profile;
import com.alpha_code.alpha_code_user_service.entity.Role;
import com.alpha_code.alpha_code_user_service.exception.AuthenticationException;
import com.alpha_code.alpha_code_user_service.exception.ConflictException;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.mapper.AccountMapper;
import com.alpha_code.alpha_code_user_service.mapper.ProfileMapper;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.ProfileRepository;
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
import java.util.List;
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
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public LoginDto.LoginResponse login(LoginDto.LoginRequest loginRequest) {
        Optional<Account> accountOptional = repository.findAccountByUsername(loginRequest.getUsername());
        if (accountOptional.isEmpty()) {
            accountOptional = repository.findByEmail(loginRequest.getUsername());
        }

        Account account = accountOptional.orElseThrow(() ->
                new AuthenticationException("Sai tài khoản hoặc mật khẩu"));

        if (!passwordEncoder.matches(loginRequest.getPassword().trim(), account.getPassword())) {
            throw new AuthenticationException("Sai tài khoản hoặc mật khẩu");
        }

        // Nếu account bị khóa thì không cho login
        if (account.getStatus() == 0) {
            throw new AuthenticationException("Tài khoản đang bị khóa");
        }

        // Kiểm tra role của account
        String accountRoleName = account.getRole().getName(); // Admin/Staff/User

        // Nếu là Admin hoặc Staff → login như bình thường (trả full token)
        if ("Admin".equalsIgnoreCase(accountRoleName) || "Staff".equalsIgnoreCase(accountRoleName)) {
            String accessToken = jwtUtil.generateAccessToken(account);
            String refreshToken = jwtUtil.generateRefreshToken(account);

            redisRefreshTokenService.save(
                    account.getId(),
                    refreshToken,
                    refreshTokenExpirationMs,
                    TimeUnit.MILLISECONDS
            );

            dashboardService.addOnlineUser(account.getId());

            return LoginDto.LoginResponse.builder()
                    .requiresProfile(false)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .profiles(null)
                    .build();
        }

        // Nếu là User → kiểm tra profile
        List<Profile> profiles = profileRepository.findByAccountId(account.getId());
        if (profiles == null || profiles.isEmpty()) {
            // User chưa có profile → FE sẽ chuyển tới tạo profile cha
            return LoginDto.LoginResponse.builder()
                    .requiresProfile(true)
                    .profiles(List.of())
                    .accessToken(null)
                    .refreshToken(null)
                    .build();
        }

        // User có profile → yêu cầu FE chọn profile
        List<ProfileDto> profileDtos = profiles.stream()
                .map(ProfileMapper::toDto)
                .toList();

        return LoginDto.LoginResponse.builder()
                .requiresProfile(true)
                .profiles(profileDtos)
                .accessToken(null)
                .refreshToken(null)
                .build();
    }


    @Override
    public AccountDto register(LoginDto.RegisterRequest registerRequest) {
        if (repository.existsByUsername(registerRequest.getUsername())) {
            throw new ConflictException("Tên đăng nhập đã được sử dụng");
        }
        if (repository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email đã được sử dụng");
        }
        if (repository.existsByPhone(registerRequest.getPhone())) {
            throw new ConflictException("Số điện thoại đã được sử dụng");
        }

        Account entity = new Account();
        entity.setUsername(registerRequest.getUsername());
        entity.setPassword(passwordEncoder.encode(registerRequest.getPassword().trim()));
        entity.setFullName(registerRequest.getFullName());
        entity.setEmail(registerRequest.getEmail());
        entity.setPhone(registerRequest.getPhone());
        entity.setGender(registerRequest.getGender());
        entity.setStatus(1);
        var roleUser = roleRepository.findByNameIgnoreCase("User");
        if (roleUser.isEmpty()) {
            Role newRole = new Role();
            newRole.setName("User");
            newRole.setStatus(1);
            roleRepository.save(newRole);
            roleUser = roleRepository.findByNameIgnoreCase("User");
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
            String picture = firebaseToken.getPicture();

            Account account = repository.findByEmail(email).orElse(null);

            // Nếu không có account -> tạo mới với Role User
            if (account == null) {
                account = new Account();
                account.setUsername(email);
                account.setPassword(passwordEncoder.encode("")); // đặt trống
                account.setFullName(name);
                account.setEmail(email);
                account.setPhone("");
                account.setGender(0);
                account.setImage(picture);
                account.setCreatedDate(LocalDateTime.now());
                account.setStatus(1);

                var roleUser = roleRepository.findByNameIgnoreCase("User");
                if (roleUser.isEmpty()) {
                    Role newRole = new Role();
                    newRole.setName("User");
                    newRole.setStatus(1);
                    roleRepository.save(newRole);
                    roleUser = roleRepository.findByNameIgnoreCase("User");
                }

                account.setRoleId(roleUser.get().getId());
                account.setRole(roleUser.get());
                account = repository.save(account);
            }

            // Kiểm tra role
            String accountRoleName = account.getRole().getName();

            // Nếu là Admin hoặc Staff → trả token full
            if ("Admin".equalsIgnoreCase(accountRoleName) || "Staff".equalsIgnoreCase(accountRoleName)) {
                String accessToken = jwtUtil.generateAccessToken(account);
                String refreshToken = jwtUtil.generateRefreshToken(account);

                redisRefreshTokenService.save(
                        account.getId(),
                        refreshToken,
                        refreshTokenExpirationMs,
                        TimeUnit.MILLISECONDS
                );

                dashboardService.addOnlineUser(account.getId());

                return LoginDto.LoginResponse.builder()
                        .requiresProfile(false)
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .profiles(null)
                        .build();
            }

            // Nếu là User → kiểm tra profile
            List<Profile> profiles = profileRepository.findByAccountId(account.getId());
            if (profiles == null || profiles.isEmpty()) {
                // Chưa có profile → FE sẽ chuyển hướng tới tạo profile cha
                return LoginDto.LoginResponse.builder()
                        .requiresProfile(true)
                        .profiles(List.of())
                        .accessToken(null)
                        .refreshToken(null)
                        .build();
            }

            // Có profile → yêu cầu chọn profile
            List<ProfileDto> profileDtos = profiles.stream()
                    .map(ProfileMapper::toDto)
                    .toList();

            return LoginDto.LoginResponse.builder()
                    .requiresProfile(true)
                    .profiles(profileDtos)
                    .accessToken(null)
                    .refreshToken(null)
                    .build();

        } catch (Exception e) {
            throw new AuthenticationException("Đăng nhập google thất bại: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public boolean requestResetPassword(String email) throws MessagingException {
        var account = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy email trong hệ thống"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Token không hợp lệ hoặc đã hết hạn"));

        // 2. Check token is valid or not
        if (!jwtUtil.validateJwtToken(dto.getResetToken())) {
            throw new IllegalArgumentException("Token không hợp lệ hoặc đã hết hạn");
        }

        // 3. Hash new password
        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        // 4. Save new password to db
        repository.save(account);
        return true;
    }

    @Override
    public LoginDto.LoginResponse switchProfile(SwitchProfileRequest request) {
        // 1. Tìm profile thuộc account này
        Profile profile = profileRepository.findByIdAndAccountId(request.getProfileId(), request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy profile"));

        // 2. Nếu profile là trẻ em và có passcode thì kiểm tra
        if (profile.getPassCode() != null && !profile.getPassCode().equals(request.getPassCode())) {
            throw new IllegalArgumentException("PassCode không đúng");
        }

        // 3. Lấy role của profile
        Role role = roleRepository.findById(profile.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role"));

        Account account = accountRepository.findById(profile.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy account"));

        account.setRoleId(role.getId());

        // 4. Sinh token mới (gắn profileId + role vào claim)
        String accessToken = jwtUtil.generateAccessToken(account);
        String refreshToken = jwtUtil.generateRefreshToken(account);

        // 5. Trả về access & refresh token
        return LoginDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
