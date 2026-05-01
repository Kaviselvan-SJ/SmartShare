package com.smartshare.exception.analytics;

public class AnalyticsTrackingException extends RuntimeException {
    public AnalyticsTrackingException(String message) {
        super(message);
    }

    public AnalyticsTrackingException(String message, Throwable cause) {
        super(message, cause);
    }
}
