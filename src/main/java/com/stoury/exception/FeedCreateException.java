package com.stoury.exception;

public class FeedCreateException extends RuntimeException {
    public FeedCreateException() {
    }

    public FeedCreateException(String message) {
        super(message);
    }

    public FeedCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeedCreateException(Throwable cause) {
        super(cause);
    }
}
