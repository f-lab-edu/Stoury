package com.stoury.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Values {
    public static final String MAX_LONG = "9223372036854775807";

    public static final long MAX_FOLLOWING = 5000;

    public static final String MEMBER_ID_NOT_NULL_MESSAGE = "Member id cannot be null.";
    public static final String FEED_ID_NOT_NULL_MESSAGE = "Feed id cannot be null.";
}
