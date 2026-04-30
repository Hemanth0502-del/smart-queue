package com.smartqueue.dto;

import com.smartqueue.entity.PriorityType;
import jakarta.validation.constraints.NotNull;

public class TokenRequest {

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Counter is required")
    private Long counterId;

    @NotNull(message = "Priority type is required")
    private PriorityType priorityType = PriorityType.NORMAL;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCounterId() {
        return counterId;
    }

    public void setCounterId(Long counterId) {
        this.counterId = counterId;
    }

    public PriorityType getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(PriorityType priorityType) {
        this.priorityType = priorityType;
    }
}
