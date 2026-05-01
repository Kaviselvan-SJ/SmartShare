package com.smartshare.exception.shortlink;

public class ShortLinkCreationException extends RuntimeException {
    public ShortLinkCreationException(String message) {
        super(message);
    }

    public ShortLinkCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
