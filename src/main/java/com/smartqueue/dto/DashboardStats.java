package com.smartqueue.dto;

public record DashboardStats(
        long totalTokens,
        long waitingTokens,
        long servingTokens,
        long completedTokens,
        long activeCounters
) {
}
