package com.stoury.exception;

public class AlreadyLikedFeedException extends RuntimeException {
    public AlreadyLikedFeedException(String message) {
        super(message);
    }
}
