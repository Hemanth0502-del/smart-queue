package com.smartqueue.dto;

import com.smartqueue.entity.Category;
import com.smartqueue.entity.Token;
import java.util.List;

public record LiveQueueDashboard(
        List<Category> categories,
        Category selectedCategory,
        List<LiveCounterStatus> counters,
        List<Token> nextWaitingTokens,
        long totalWaitingCount,
        double averageEstimatedWaitMinutes
) {
}
