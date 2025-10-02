package com.alpha_code.alpha_code_user_service.service.impl;

import com.alpha_code.alpha_code_user_service.dto.AccountDto;
import com.alpha_code.alpha_code_user_service.dto.PagedResult;
import com.alpha_code.alpha_code_user_service.entity.Account;
import com.alpha_code.alpha_code_user_service.entity.Role;
import com.alpha_code.alpha_code_user_service.exception.AuthenticationException;
import com.alpha_code.alpha_code_user_service.exception.ConflictException;
import com.alpha_code.alpha_code_user_service.exception.ResourceNotFoundException;
import com.alpha_code.alpha_code_user_service.mapper.AccountMapper;
import com.alpha_code.alpha_code_user_service.repository.AccountRepository;
import com.alpha_code.alpha_code_user_service.repository.RoleRepository;
import com.alpha_code.alpha_code_user_service.service.AccountService;
import com.alpha_code.alpha_code_user_service.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import user.User.AccountInformation;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repository;
    private final RoleRepository roleRepository;
    private final S3Service s3Service;
    private final BCryptPasswordEncoder passwordEncoder;


    private static final String DEFAULT_ROLE = "USER";

    @Override
    @Cacheable(value = "grpc_account", key = "{#id}")
    public AccountInformation findAccountByIdGrpc(UUID id) {
        var account = repository.findAccountByIdGrpc(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

        return AccountMapper.toAccountInformationGrpc(account);
    }


    @Override
    @Cacheable(value = "accounts_list", key = "{#page, #size, #status}")
    public PagedResult<AccountDto> getAll(int page, int size, Integer status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Account> pageResult;

        if (status != null) {
            pageResult = repository.findAllByStatus(status, pageable);
        } else {
            pageResult = repository.findAll(pageable);
        }
        return new PagedResult<>(pageResult.map(AccountMapper::toDto));
    }

    @Override
    @Cacheable(value = "accounts", key = "#id")
    public AccountDto getById(UUID id) {
        var account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        return AccountMapper.toDto(account);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"accounts_list"}, allEntries = true)
    public AccountDto create(AccountDto accountDto, MultipartFile avatarFile) {
        if (repository.existsByUsername(accountDto.getUsername())) {
            throw new ConflictException("Tên đăng nhập đã được sử dụng");
        }
        if (repository.existsByEmail(accountDto.getEmail())) {
            throw new ConflictException("Email đã được sử dụng");
        }
        if (repository.existsByPhone(accountDto.getPhone())) {
            throw new ConflictException("Số điện thoại đã được sử dụng");
        }

        Account entity = AccountMapper.toEntity(accountDto);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setStatus(1);
        entity.setBannedReason(null);
        entity.setPassword(passwordEncoder.encode(accountDto.getPassword()));

        if (accountDto.getRoleId() != null) {
            Role role = roleRepository.findById(accountDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Vai trò"));
            entity.setRole(role);
        } else {
            Optional<Role> roleOpt = roleRepository.findByNameIgnoreCase(DEFAULT_ROLE);
            Role role;
            if (roleOpt.isEmpty()) {
                role = new Role();
                role.setName(DEFAULT_ROLE);
                role.setStatus(1);
                roleRepository.save(role);
            } else {
                role = roleOpt.get();
            }
            entity.setRole(role);
            entity.setRoleId(role.getId());
        }
        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String fileKey = "avatars/" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                String avatarUrl = s3Service.uploadBytes(avatarFile.getBytes(), fileKey, avatarFile.getContentType());
                entity.setImage(avatarUrl);
            }

            Account savedEntity = repository.save(entity);
            return AccountMapper.toDto(savedEntity);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo Tài khoản", e);
        }
    }

    @Override
    @Transactional
    @CachePut(value = "accounts", key = "#id")
    @CacheEvict(value = {"accounts_list", "grpc_account"}, allEntries = true)
    public AccountDto update(UUID id, AccountDto accountDto) {
        Account existingAccount = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Tài khoản"));

        existingAccount.setUsername(accountDto.getUsername());
        existingAccount.setPassword(accountDto.getPassword());
        existingAccount.setFullName(accountDto.getFullName());
        existingAccount.setEmail(accountDto.getEmail());
        existingAccount.setPhone(accountDto.getPhone());
        existingAccount.setGender(accountDto.getGender());
        existingAccount.setRoleId(accountDto.getRoleId());
        existingAccount.setLicenseId(accountDto.getLicenseId());
        existingAccount.setStatus(accountDto.getStatus());
        existingAccount.setLastUpdated(LocalDateTime.now());

        Account updatedEntity = repository.save(existingAccount);
        return AccountMapper.toDto(updatedEntity);
    }

    @Override
    @Transactional
    @CachePut(value = "accounts", key = "#id")
    @CacheEvict(value = {"accounts_list", "grpc_account"}, allEntries = true)
    public AccountDto updateProfile(UUID id, AccountDto accountDto, MultipartFile avatarFile) {
        Account existingAccount = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Tài khoản"));

        existingAccount.setUsername(accountDto.getUsername());
        existingAccount.setPassword(accountDto.getPassword());
        existingAccount.setFullName(accountDto.getFullName());
        existingAccount.setEmail(accountDto.getEmail());
        existingAccount.setPhone(accountDto.getPhone());
        existingAccount.setGender(accountDto.getGender());
        existingAccount.setRoleId(accountDto.getRoleId());
        existingAccount.setLicenseId(accountDto.getLicenseId());
        existingAccount.setStatus(accountDto.getStatus());
        existingAccount.setLastUpdated(LocalDateTime.now());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String fileKey = "avatars/" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                String avatarUrl = s3Service.uploadBytes(avatarFile.getBytes(), fileKey, avatarFile.getContentType());
                existingAccount.setImage(avatarUrl);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi tải lên ảnh đại diện", e);
            }

        }
        Account updatedEntity = repository.save(existingAccount);
        return AccountMapper.toDto(updatedEntity);
    }

    @Override
    @Transactional
    @CachePut(value = "accounts", key = "#id")
    @CacheEvict(value = {"accounts_list", "grpc_account"}, allEntries = true)
    public AccountDto patchUpdate(UUID id, AccountDto accountDto) {
        Account existingAccount = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Tài khoản"));

        if (accountDto.getUsername() != null) {
            existingAccount.setUsername(accountDto.getUsername());
        }
        if (accountDto.getPassword() != null) {
            existingAccount.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        }
        if (accountDto.getFullName() != null) {
            existingAccount.setFullName(accountDto.getFullName());
        }
        if (accountDto.getPhone() != null) {
            existingAccount.setPhone(accountDto.getPhone());
        }
        if (accountDto.getGender() != null) {
            existingAccount.setGender(accountDto.getGender());
        }
        if (accountDto.getEmail() != null) {
            existingAccount.setEmail(accountDto.getEmail());
        }
        if (accountDto.getRoleId() != null) {
            existingAccount.setRoleId(accountDto.getRoleId());
        }
        existingAccount.setLastUpdated(LocalDateTime.now());

        Account updatedEntity = repository.save(existingAccount);
        return AccountMapper.toDto(updatedEntity);

    }

    @Override
    @Transactional
    @CachePut(value = "accounts", key = "#id")
    @CacheEvict(value = {"accounts_list", "grpc_account"}, allEntries = true)
    public AccountDto patchUpdateProfile(UUID id, AccountDto accountDto, MultipartFile avatarFile) {
        Account existingAccount = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Tài khoản"));

        if (accountDto.getUsername() != null) {
            existingAccount.setUsername(accountDto.getUsername());
        }
        if (accountDto.getPassword() != null) {
            existingAccount.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        }
        if (accountDto.getFullName() != null) {
            existingAccount.setFullName(accountDto.getFullName());
        }
        if (accountDto.getEmail() != null) {
            existingAccount.setEmail(accountDto.getEmail());
        }
        if (accountDto.getPhone() != null) {
            existingAccount.setPhone(accountDto.getPhone());
        }
        if (accountDto.getGender() != null) {
            existingAccount.setGender(accountDto.getGender());
        }
        if (accountDto.getLicenseId() != null) {
            existingAccount.setLicenseId(accountDto.getLicenseId());
        }
        if (accountDto.getStatus() != null) {
            existingAccount.setStatus(accountDto.getStatus());
        }
        if (accountDto.getRoleId() != null) {
            Role role = roleRepository.findById(accountDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Vai trò"));
            existingAccount.setRole(role);
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String fileKey = "avatars/" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                String avatarUrl = s3Service.uploadBytes(avatarFile.getBytes(), fileKey, avatarFile.getContentType());
                existingAccount.setImage(avatarUrl);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi tải lên ảnh đại diện", e);
            }

        }

        existingAccount.setLastUpdated(LocalDateTime.now());

        Account updatedEntity = repository.save(existingAccount);
        return AccountMapper.toDto(updatedEntity);
    }

    @Override
    @Transactional
    @CachePut(value = "accounts", key = "#id")
    @CacheEvict(value = {"accounts_list", "grpc_account"}, allEntries = true)
    public AccountDto changePassword(UUID id, String oldPassword, String newPassword) {
        Account existingAccount = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Tài khoản"));

        if (!passwordEncoder.matches(oldPassword, existingAccount.getPassword())) {
            throw new AuthenticationException("Old password is incorrect");
        }

        existingAccount.setPassword(passwordEncoder.encode(newPassword));
        existingAccount.setLastUpdated(LocalDateTime.now());
        Account updatedEntity = repository.save(existingAccount);
        return AccountMapper.toDto(updatedEntity);
    }

    @Override
    @Transactional
    @CachePut(value = "accounts", key = "#id")
    @CacheEvict(value = {"accounts_list", "grpc_account"}, allEntries = true)
    public AccountDto changeStatus(UUID id, Integer status) {
        Account existingAccount = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Tài khoản"));

        existingAccount.setStatus(status);
        existingAccount.setLastUpdated(LocalDateTime.now());
        Account updatedEntity = repository.save(existingAccount);
        return AccountMapper.toDto(updatedEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"accounts", "accounts_list", "grpc_account"}, allEntries = true)
    public String delete(UUID id) {
        try {
            Account account = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Tài khoản"));

//        repository.delete(id);
            account.setStatus(0);
            account.setLastUpdated(LocalDateTime.now());
            repository.save(account);
            return "Xoá thành công Account với ID: " + id;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xoá Account", e);
        }

    }

    @Override
    public AccountDto findAccountByFullName(String fullName) {
        var account = repository.findAccountByFullName(fullName);
        return AccountMapper.toDto(account);
    }
}
