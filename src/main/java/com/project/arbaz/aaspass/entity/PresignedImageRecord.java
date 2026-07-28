package com.project.arbaz.aaspass.entity;

import com.project.arbaz.aaspass.enums.PresignedImageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "presigned_image_record")
public class PresignedImageRecord {
    @Id
    private String presignedImageRecordId;

    @Column(nullable = false)
    private Long imageSize;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PresignedImageStatus status;

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PresignedImageStatus.PENDING;
        }
    }
}
