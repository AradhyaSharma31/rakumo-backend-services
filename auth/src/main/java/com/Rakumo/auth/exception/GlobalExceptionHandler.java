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

package com.rakumo.auth.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalExceptionHandler {

    public StatusRuntimeException handleException(Throwable throwable) {
        log.error("Auth Service Exception: ", throwable);

        if (throwable instanceof UserAlreadyExistsException) {
            return Status.ALREADY_EXISTS
                    .withDescription(throwable.getMessage())
                    .withCause(throwable)
                    .asRuntimeException();
        }

        if (throwable instanceof TokenRefreshException) {
            return Status.UNAUTHENTICATED
                    .withDescription(throwable.getMessage())
                    .withCause(throwable)
                    .asRuntimeException();
        }

        if (throwable instanceof AuthException) {
            return Status.INVALID_ARGUMENT
                    .withDescription(throwable.getMessage())
                    .withCause(throwable)
                    .asRuntimeException();
        }

        if (throwable instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT
                    .withDescription(throwable.getMessage())
                    .withCause(throwable)
                    .asRuntimeException();
        }

        return Status.INTERNAL
                .withDescription("Internal server error")
                .withCause(throwable)
                .asRuntimeException();
    }
}
