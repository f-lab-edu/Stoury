package com.stoury.exception.member;

public class MemberCreateException extends RuntimeException {
    public MemberCreateException() {
    }

    public MemberCreateException(String message) {
        super(message);
    }

    public MemberCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
