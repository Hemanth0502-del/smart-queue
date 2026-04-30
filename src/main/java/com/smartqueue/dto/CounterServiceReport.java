package com.smartqueue.dto;

public record CounterServiceReport(
        String counterCode,
        String counterName,
        String categoryName,
        long serviceCount,
        double averageServiceMinutes
) {
}
