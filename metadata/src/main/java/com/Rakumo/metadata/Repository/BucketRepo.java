package com.Rakumo.metadata.Repository;

import com.Rakumo.metadata.Models.Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BucketRepo extends JpaRepository<Bucket, UUID> {
}
