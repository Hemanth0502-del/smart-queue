package com.smartqueue.service;

import com.smartqueue.dto.CategoryForm;
import com.smartqueue.dto.CounterForm;
import com.smartqueue.entity.Category;
import com.smartqueue.entity.Counter;
import com.smartqueue.entity.CounterStatus;
import com.smartqueue.repository.CategoryRepository;
import com.smartqueue.repository.CounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminManagementService {

    private final CategoryRepository categoryRepository;
    private final CounterRepository counterRepository;

    public AdminManagementService(CategoryRepository categoryRepository, CounterRepository counterRepository) {
        this.categoryRepository = categoryRepository;
        this.counterRepository = counterRepository;
    }

    @Transactional
    public void createCategory(CategoryForm form) {
        String name = form.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("A category with this name already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(clean(form.getDescription()));
        category.setTokenPrefix(resolveTokenPrefix(form.getTokenPrefix(), name));
        category.setActive(true);
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Long id, CategoryForm form) {
        Category category = getCategory(id);
        String name = form.getName().trim();
        categoryRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A category with this name already exists");
                });

        category.setName(name);
        category.setDescription(clean(form.getDescription()));
        category.setTokenPrefix(resolveTokenPrefix(form.getTokenPrefix(), name));
    }

    @Transactional
    public void setCategoryActive(Long id, boolean active) {
        Category category = getCategory(id);
        category.setActive(active);
    }

    @Transactional
    public void createCounter(CounterForm form) {
        String code = form.getCode().trim().toUpperCase();
        if (counterRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("A counter with this code already exists");
        }

        Counter counter = new Counter();
        applyCounterForm(counter, form, code);
        counter.setActive(true);
        counterRepository.save(counter);
    }

    @Transactional
    public void updateCounter(Long id, CounterForm form) {
        Counter counter = getCounter(id);
        String code = form.getCode().trim().toUpperCase();
        counterRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A counter with this code already exists");
                });
        applyCounterForm(counter, form, code);
    }

    @Transactional
    public void setCounterStatus(Long id, CounterStatus status) {
        Counter counter = getCounter(id);
        counter.setStatus(status);
        counter.setActive(status != CounterStatus.CLOSED);
    }

    private void applyCounterForm(Counter counter, CounterForm form, String code) {
        Category category = getCategory(form.getCategoryId());
        counter.setName(form.getName().trim());
        counter.setCode(code);
        counter.setCategory(category);
        counter.setStatus(form.getStatus());
        counter.setAverageServiceTimeInMinutes(form.getAverageServiceTimeInMinutes());
        counter.setActive(form.getStatus() != CounterStatus.CLOSED);
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    private Counter getCounter(Long id) {
        return counterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found"));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String resolveTokenPrefix(String requestedPrefix, String categoryName) {
        String prefix = clean(requestedPrefix);
        if (prefix == null) {
            // Prefix is data-driven: custom categories get a readable token prefix without code changes.
            prefix = categoryName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            return prefix.length() < 3 ? (prefix + "SQM").substring(0, 3) : prefix.substring(0, 3);
        }
        return prefix.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
