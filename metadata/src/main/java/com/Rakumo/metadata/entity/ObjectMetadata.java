///*
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package com.rakumo.metadata.entity;
//
//import jakarta.persistence.CascadeType;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.OneToMany;
//import jakarta.persistence.Table;
//import java.time.Instant;
//import java.util.UUID;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
///**
// * Entity for managing the object metadata.
// */
//@Table(name = "object_metadata")
//@Entity
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//public class ObjectMetadata {
//
//  @Id
//  private UUID objectId;
//  private String objectKey;
//  private String latestVersionId;
//  private String latestEtag;
//  private Long latestSize;
//  private Instant createdAt;
//  private Instant updatedAt;
//  private boolean isDeleted;
//
//  @ManyToOne
//  @JoinColumn(name = "bucket_id")
//  private Bucket bucket;
//
//  @OneToMany(mappedBy = "object", cascade = CascadeType.ALL)
//  private List<ObjectVersion> versions;
//}
