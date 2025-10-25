package com.alpha_code.alpha_code_user_service.repository;

import com.alpha_code.alpha_code_user_service.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    @Query("""
        SELECT p 
        FROM Profile p
        WHERE (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:accountId IS NULL OR p.accountId = :accountId)
          AND (:status IS NULL OR p.status = :status)
          AND (:isKid IS NULL OR p.isKid = :isKid)
          AND (:passCode IS NULL OR :passCode = '' OR p.passCode = :passCode)
    """)
    Page<Profile> searchProfiles(
            @Param("name") String name,
            @Param("accountId") UUID accountId,
            @Param("status") Integer status,
            @Param("isKid") Boolean isKid,
            @Param("passCode") String passCode,
            Pageable pageable
    );

    List<Profile> findListByAccountIdAndStatus(UUID accountId, Integer status);

    Optional<Profile> findByIdAndAccountId(UUID id, UUID accountId);

    List<Profile> findByAccountId(UUID accountId);
}
