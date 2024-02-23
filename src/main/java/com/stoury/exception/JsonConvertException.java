package com.stoury.exception;

public class JsonConvertException extends RuntimeException {

    public JsonConvertException(Object source, Throwable e) {
        super("Cannot convert from source:%s".formatted(source.toString()), e);
    }
}
