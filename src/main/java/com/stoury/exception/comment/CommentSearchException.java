package com.stoury.exception.comment;

public class CommentSearchException extends RuntimeException {
    public CommentSearchException() {
        this("Cannot find comment");
    }

    public CommentSearchException(String message) {
        super(message);
    }
}
