package com.stoury.exception.member;

public class MemberSearchException extends RuntimeException {
    public MemberSearchException() {
    }

    public MemberSearchException(String message) {
        super(message);
    }
}
