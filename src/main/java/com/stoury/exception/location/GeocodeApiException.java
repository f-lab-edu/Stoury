package com.stoury.exception.location;

public class GeocodeApiException extends RuntimeException {
    public GeocodeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
