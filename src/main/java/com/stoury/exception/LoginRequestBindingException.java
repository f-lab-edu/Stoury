package com.stoury.exception;

public class LoginRequestBindingException extends RuntimeException {
    public LoginRequestBindingException(Throwable cause) {
        super("Request body cannot be bind to LoginRequest", cause);
    }
}
