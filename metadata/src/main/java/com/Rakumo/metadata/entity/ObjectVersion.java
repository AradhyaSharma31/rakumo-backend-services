/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.rakumo.metadata.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity for managing the object version data.
 */
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
  private Long size;
  private String contentType;
  private Instant createdAt;
  private Boolean isDeleteMarker;
  private String storageClass;

  @ManyToOne
  @JoinColumn(name = "object_id")
  private ObjectMetadata object;

  @OneToMany(mappedBy = "objectVersion", cascade = CascadeType.ALL)
  private List<CustomMetadata> customMetadata;
}
