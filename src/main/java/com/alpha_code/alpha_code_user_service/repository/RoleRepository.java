package com.alpha_code.alpha_code_user_service.repository;

import com.alpha_code.alpha_code_user_service.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Role findByName(String name);

    Optional<Role> findByNameIgnoreCase(String name);

    Page<Role> findAllByStatus(Integer status, Pageable pageable);
}
