package com.alpha_code.alpha_code_user_service.repository;

import com.alpha_code.alpha_code_user_service.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Page<Account> findAllByStatus(Integer status, Pageable pageable);

    Account findAccountByFullName(String fullName);

    Optional<Account> findAccountByUsername(String username);

    Optional<Account> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByRoleId(UUID roleId);

    long countByRoleIdAndCreatedDateBetween(UUID roleId, LocalDateTime start, LocalDateTime end);

    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findAccountByIdGrpc(@Param("id") UUID id);
}
