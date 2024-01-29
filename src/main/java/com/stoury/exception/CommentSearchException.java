package com.stoury.exception;

public class CommentSearchException extends RuntimeException {
    public CommentSearchException() {
        this("Cannot find comment");
    }

    public CommentSearchException(String message) {
        super(message);
    }
}
