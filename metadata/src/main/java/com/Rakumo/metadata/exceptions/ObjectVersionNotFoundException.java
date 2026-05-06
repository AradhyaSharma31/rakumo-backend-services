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

package com.rakumo.metadata.exceptions;

import java.io.Serial;
import lombok.Getter;

/**
 * Exception thrown when a specific version of an object is not found in the metadata store.
 */
@Getter
public class ObjectVersionNotFoundException extends Exception {

  @Serial
  private static final long serialVersionUID = -3263969709652849785L;

  /**
   * Constructs a new ObjectVersionNotFoundException with the specified detail message.
   *
   * @param message the detail message explaining the reason for the exception
   */
  public ObjectVersionNotFoundException(String message) {
    super(message);
  }
}
