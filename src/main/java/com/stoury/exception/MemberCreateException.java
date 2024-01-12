package com.stoury.exception;

public class MemberCreateException extends RuntimeException {
    public MemberCreateException(RuntimeException e) {
        super(e);
    }
}
