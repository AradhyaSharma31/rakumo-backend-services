package com.Rakumo.metadata.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Table(name = "bucket")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Bucket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID bucketId;
    private UUID ownerId;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean versioningEnabled;
    private String region;

    @OneToMany(mappedBy = "bucket", cascade = CascadeType.ALL)
    private List<ObjectMetadata> objects;

}
