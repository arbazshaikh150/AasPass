package com.project.arbaz.aaspass.repository;

import com.project.arbaz.aaspass.entity.PresignedImageRecord;
import com.project.arbaz.aaspass.enums.PresignedImageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
// TODO : INDEXING IS PENDING
public interface PresignedImageRecordRepository extends JpaRepository<PresignedImageRecord, String> {
    List<PresignedImageRecord> findByCreatedBy(Long createdBy);

    List<PresignedImageRecord> findByCreatedByAndStatus(Long createdBy, PresignedImageStatus status);

    @Query("""
            SELECT p
            FROM PresignedImageRecord p
            WHERE p.presignedImageRecordId IN :ids
              AND p.status = :status
            """)
    List<PresignedImageRecord> findByIdsAndStatus(
            @Param("ids") Collection<String> ids,
            @Param("status") PresignedImageStatus status
    );

    @Query("""
            SELECT p.presignedImageRecordId
            FROM PresignedImageRecord p
            WHERE p.status = :pendingStatus
              AND p.createdAt <= :createdAt
            ORDER BY p.createdAt ASC
            """)
    List<String> findExpiredPendingRecordsForUpdate(
            @Param("createdAt") LocalDateTime createdAt,
            @Param("pendingStatus") PresignedImageStatus pendingStatus
    );

    @Modifying
    @Query("""
            UPDATE PresignedImageRecord p
            SET p.status = :expiredStatus
            WHERE p.presignedImageRecordId IN :ids
              AND p.status = :pendingStatus
            """)
    int markPendingRecordsExpiredByIds(
            @Param("ids") Collection<String> ids,
            @Param("pendingStatus") PresignedImageStatus pendingStatus,
            @Param("expiredStatus") PresignedImageStatus expiredStatus
    );

}
