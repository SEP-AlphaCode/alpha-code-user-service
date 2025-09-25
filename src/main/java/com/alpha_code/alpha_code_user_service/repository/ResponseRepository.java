package com.alpha_code.alpha_code_user_service.repository;

import com.alpha_code.alpha_code_user_service.entity.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResponseRepository extends JpaRepository<Response, UUID> {
    @Query("SELECT r FROM Response r " +
            "WHERE (:keyword IS NULL OR LOWER(r.responseContent) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:requestId IS NULL OR r.requestId = :requestId) " +
            "AND (:responderId IS NULL OR r.responderId = :responderId)")
    Page<Response> search(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("requestId") UUID requestId,
            @Param("responderId") UUID responderId,
            Pageable pageable
    );
}
