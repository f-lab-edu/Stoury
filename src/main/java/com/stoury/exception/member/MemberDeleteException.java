package com.stoury.exception.member;

public class MemberDeleteException extends RuntimeException {
    public MemberDeleteException() {
    }

    public MemberDeleteException(String message) {
        super(message);
    }

    public MemberDeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public static MemberDeleteException causeByMemberNotFound() {
        return new MemberDeleteException("Cannot find the member");
    }
}
