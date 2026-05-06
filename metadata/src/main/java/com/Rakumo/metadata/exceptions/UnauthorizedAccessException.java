package com.Rakumo.metadata.Exceptions;

import io.grpc.Status;
import lombok.Getter;

@Getter
public class UnauthorizedAccessException extends RuntimeException {
    private final Status status;

    public UnauthorizedAccessException(String message) {
        super(message);
        this.status = Status.PERMISSION_DENIED.withDescription(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
        this.status = Status.PERMISSION_DENIED.withDescription(message);
    }

}
