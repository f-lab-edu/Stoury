package com.stoury.exception;

import com.stoury.utils.Values;

public class MaxFollowingException extends RuntimeException {
    public static final String ERR_MESSAGE = "Can't follow more than " + Values.MAX_FOLLOWING + "members.";

    public MaxFollowingException(String message) {
        super(message);
    }

    public MaxFollowingException() {
        this(ERR_MESSAGE);
    }
}
