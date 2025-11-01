package com.Rakumo.auth.exception;

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