package com.smartqueue.dto;

import com.smartqueue.entity.CounterStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CounterForm {

    @NotBlank(message = "Counter name is required")
    @Size(max = 120, message = "Counter name must be 120 characters or fewer")
    private String name;

    @NotBlank(message = "Counter code is required")
    @Size(max = 40, message = "Counter code must be 40 characters or fewer")
    private String code;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Status is required")
    private CounterStatus status = CounterStatus.OPEN;

    @Min(value = 1, message = "Average service time must be at least 1 minute")
    @Max(value = 480, message = "Average service time must be 480 minutes or fewer")
    private int averageServiceTimeInMinutes = 10;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public CounterStatus getStatus() {
        return status;
    }

    public void setStatus(CounterStatus status) {
        this.status = status;
    }

    public int getAverageServiceTimeInMinutes() {
        return averageServiceTimeInMinutes;
    }

    public void setAverageServiceTimeInMinutes(int averageServiceTimeInMinutes) {
        this.averageServiceTimeInMinutes = averageServiceTimeInMinutes;
    }
}
