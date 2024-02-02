package com.stoury.exception;

public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException() {
        this("Not allowed");
    }

    public NotAuthorizedException(String message) {
        super(message);
    }
}
