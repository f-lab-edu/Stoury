package com.stoury.exception.member;

public class MemberSearchException extends RuntimeException {
    public MemberSearchException() {
        this("Cannot find Member");
    }

    public MemberSearchException(String message) {
        super(message);
    }
}
