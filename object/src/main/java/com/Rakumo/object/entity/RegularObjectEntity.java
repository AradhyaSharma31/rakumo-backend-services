package com.Rakumo.object.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "regular_objects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegularObjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "version_id")
    private String versionId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "physical_path")
    private String physicalPath;
}