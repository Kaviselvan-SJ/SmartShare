package com.smartshare.exception.tagging.search;

public class TagSearchException extends RuntimeException {
    public TagSearchException(String message) {
        super(message);
    }

    public TagSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
