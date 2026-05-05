package com.smartshare.exception.file;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class PreviewAccessException extends RuntimeException {
    public PreviewAccessException(String message) {
        super(message);
    }

    public PreviewAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
