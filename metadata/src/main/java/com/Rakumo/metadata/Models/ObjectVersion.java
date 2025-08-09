package com.Rakumo.metadata.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Table(name = "object_version")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ObjectVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID versionId;
    private String etag;
    private String storageLocation;
    private long size;
    private String contentType;
    private Instant createdAt;
    private boolean isDeleteMarker;
    private String storageClass;

    @ManyToOne
    @JoinColumn(name = "object_id")
    private ObjectMetadata object;

    @OneToMany(mappedBy = "objectVersion", cascade = CascadeType.ALL)
    private List<CustomMetadata> customMetadata;

}
