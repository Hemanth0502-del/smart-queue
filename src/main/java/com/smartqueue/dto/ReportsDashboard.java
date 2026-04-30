package com.smartqueue.dto;

import com.smartqueue.entity.Category;
import com.smartqueue.entity.Token;
import java.time.LocalDate;
import java.util.List;

public record ReportsDashboard(
        LocalDate reportDate,
        Long categoryId,
        List<Category> categories,
        long totalTokensToday,
        long completedTokensToday,
        long skippedTokensToday,
        long waitingTokens,
        List<CategoryTokenReport> categoryTokenCounts,
        List<CounterServiceReport> counterServiceCounts,
        List<Token> detailedTokens
) {
    public List<String> categoryLabels() {
        return categoryTokenCounts.stream()
                .map(CategoryTokenReport::categoryName)
                .toList();
    }

    public List<Long> categoryCounts() {
        return categoryTokenCounts.stream()
                .map(CategoryTokenReport::tokenCount)
                .toList();
    }

    public List<String> counterLabels() {
        return counterServiceCounts.stream()
                .map(report -> report.counterCode() + " - " + report.counterName())
                .toList();
    }

    public List<Long> counterServiceCountsData() {
        return counterServiceCounts.stream()
                .map(CounterServiceReport::serviceCount)
                .toList();
    }

    public List<Double> averageServiceMinutesData() {
        return counterServiceCounts.stream()
                .map(CounterServiceReport::averageServiceMinutes)
                .toList();
    }
}
