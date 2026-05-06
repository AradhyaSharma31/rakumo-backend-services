package com.Rakumo.metadata.Exceptions;

import lombok.Getter;

@Getter
public class ObjectVersionNotFoundException extends Exception {

    public ObjectVersionNotFoundException(String message) {
        super(message);
    }

}
