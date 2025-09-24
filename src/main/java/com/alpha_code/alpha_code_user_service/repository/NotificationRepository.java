package com.alpha_code.alpha_code_user_service.repository;

import com.alpha_code.alpha_code_user_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findAllByAccountId(UUID accountId, Pageable pageable);

    Page<Notification> findAllByAccountIdAndStatus(UUID accountId, Integer status, Pageable pageable);

    Page<Notification> findAllByStatus(Integer status, Pageable pageable);
}
