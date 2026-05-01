package com.smartshare.exception.hash;

public class HashComputationException extends RuntimeException {
    public HashComputationException(String message) {
        super(message);
    }

    public HashComputationException(String message, Throwable cause) {
        super(message, cause);
    }
}
