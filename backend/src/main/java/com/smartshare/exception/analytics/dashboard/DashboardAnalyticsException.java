package com.smartshare.exception.analytics.dashboard;

public class DashboardAnalyticsException extends RuntimeException {
    public DashboardAnalyticsException(String message) {
        super(message);
    }

    public DashboardAnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }
}
