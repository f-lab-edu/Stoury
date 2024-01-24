package com.stoury.exception.feed;

public class FeedSearchException extends RuntimeException {
    public FeedSearchException() {
        super();
    }

    public FeedSearchException(String message) {
        super(message);
    }

    public FeedSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}