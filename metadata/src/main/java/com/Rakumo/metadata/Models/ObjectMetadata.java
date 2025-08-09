package com.Rakumo.metadata.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Table(name = "object_metadata")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ObjectMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;
    private String objectKey;
    private String latestVersionId;
    private String latestEtag;
    private Long latestSize;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "bucket_id")
    private Bucket bucket;

    @OneToMany(mappedBy = "object", cascade = CascadeType.ALL)
    private List<ObjectVersion> versions;

}
