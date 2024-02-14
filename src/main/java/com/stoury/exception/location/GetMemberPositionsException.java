package com.stoury.exception.location;

public class GetMemberPositionsException extends RuntimeException {
    public GetMemberPositionsException() {
        this("Some problem occur while getting members' coordinate.");
    }

    public GetMemberPositionsException(String message) {
        super(message);
    }
}
