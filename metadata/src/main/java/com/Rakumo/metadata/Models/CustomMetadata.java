package com.Rakumo.metadata.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "custom_metadata")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;
    private String key;
    private String value;

    @ManyToOne
    @JoinColumn(name = "version_id")
    private ObjectVersion objectVersion;
}
