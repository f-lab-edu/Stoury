package com.stoury.exception;

public class MemberCrudExceptions {
    public static final String MEMBER_NOT_FOUND_MESSAGE = "Cannot find the member";
    public static class MemberCreateException extends RuntimeException {
        public MemberCreateException() {
        }

        public MemberCreateException(String message) {
            super(message);
        }

        public MemberCreateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class MemberSearchException extends RuntimeException {
        public MemberSearchException() {
        }


        public MemberSearchException(String message) {
            super(message);
        }
    }

    public static class MemberDeleteException extends RuntimeException {
        public MemberDeleteException() {
        }

        public MemberDeleteException(String message) {
            super(message);
        }

        public MemberDeleteException(String message, Throwable cause) {
            super(message, cause);
        }

        public static MemberDeleteException causeByMemberNotFound() {
            return new MemberDeleteException(MEMBER_NOT_FOUND_MESSAGE);
        }
    }

    public static class MemberUpdateException extends RuntimeException {
        public MemberUpdateException() {
        }

        public MemberUpdateException(String message) {
            super(message);
        }

        public MemberUpdateException(String message, Throwable cause) {
            super(message, cause);
        }

        public static MemberUpdateException causeByMemberNotFound() {
            return new MemberUpdateException(MEMBER_NOT_FOUND_MESSAGE);
        }
    }
}
