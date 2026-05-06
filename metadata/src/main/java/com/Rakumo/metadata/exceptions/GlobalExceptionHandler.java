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

import java.nio.file.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the application.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   *Handles BucketNotFoundException and returns a 404 Not Found response.
   *
   * @param ex the exception to handle
   * @return a ResponseEntity containing the error response
   */
  @ExceptionHandler(BucketNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBucketNotFound(BucketNotFoundException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles ObjectNotFoundException and returns a 404 Not Found response.
   *
   * @param ex the exception to handle
   * @return a ResponseEntity containing the error response
   */
  @ExceptionHandler(ObjectNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleObjectNotFound(ObjectNotFoundException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles ObjectDeletionException and returns a 500 Internal Server Error response.
   *
   * @param ex the exception to handle
   * @return a ResponseEntity containing the error response
   */
  @ExceptionHandler(ObjectDeletionException.class)
  public ResponseEntity<ErrorResponse> handleObjectDeletion(ObjectDeletionException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles ObjectVersionNotFoundException and returns a 404 Not Found response.
   *
   * @param ex the exception to handle
   * @return a ResponseEntity containing the error response
   */
  @ExceptionHandler(ObjectVersionNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleObjectVersionNotFound(ObjectVersionNotFoundException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles AccessDeniedException and returns a 403 Forbidden response.
   *
   * @param ex the exception to handle
   * @return a ResponseEntity containing the error response
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  /**
   * Handles all other exceptions and returns a 500 Internal Server Error response.
   *
   * @param ex the exception to handle
   * @return a ResponseEntity containing the error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
    log.error(ex.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred, try again later."
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * A simple record class for representing error responses in a consistent format.
   *
   * @param status the HTTP status code of the error
   * @param message a descriptive message explaining the error
   */
  public record ErrorResponse(int status, String message) {}

}
