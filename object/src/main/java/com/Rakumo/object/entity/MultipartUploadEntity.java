package com.Rakumo.object.entity;

import com.Rakumo.object.enumeration.UploadStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "multipart_uploads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadEntity {
    @Id
    private String uploadId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "final_filename")
    private String finalFilename;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UploadStatus status;
}
