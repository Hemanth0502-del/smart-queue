package com.smartqueue.dto;

import com.smartqueue.entity.Category;

public record CategoryQueueStatus(
        Category category,
        long waitingCount,
        long calledCount,
        long completedCount
) {
}
