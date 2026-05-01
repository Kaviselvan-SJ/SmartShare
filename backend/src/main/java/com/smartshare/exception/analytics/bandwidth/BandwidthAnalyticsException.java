package com.smartshare.exception.analytics.bandwidth;

public class BandwidthAnalyticsException extends RuntimeException {
    public BandwidthAnalyticsException(String message) {
        super(message);
    }

    public BandwidthAnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }
}
