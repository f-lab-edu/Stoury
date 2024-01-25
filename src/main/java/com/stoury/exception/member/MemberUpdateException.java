package com.stoury.exception.member;

public class MemberUpdateException extends RuntimeException {
    public MemberUpdateException() {
    }

    public MemberUpdateException(String message) {
        super(message);
    }

    public MemberUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public static MemberUpdateException causeByMemberNotFound() {
        return new MemberUpdateException("Cannot find the member");
    }
}
