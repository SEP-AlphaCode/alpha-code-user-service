package com.alpha_code.alpha_code_user_service.repository;

import com.alpha_code.alpha_code_user_service.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {
    @Query("""
        SELECT ur 
        FROM Request ur
        WHERE (:title IS NULL OR LOWER(ur.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:type IS NULL OR LOWER(ur.type) = LOWER(:type))
          AND (:status IS NULL OR ur.status = :status)
          AND (:accountId IS NULL OR ur.accountId = :accountId)
          AND (:rate IS NULL OR ur.rate = :rate)
    """)
    Page<Request> searchRequests(
            @Param("title") String title,
            @Param("type") String type,
            @Param("status") Integer status,
            @Param("accountId") UUID accountId,
            @Param("rate") Integer rate,
            Pageable pageable
    );
}
