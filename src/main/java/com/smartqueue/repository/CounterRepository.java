package com.smartqueue.repository;

import com.smartqueue.entity.Counter;
import com.smartqueue.entity.CounterStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterRepository extends JpaRepository<Counter, Long> {
    long countByActiveTrue();
    long countByCategoryIdAndActiveTrue(Long categoryId);
    List<Counter> findByCategoryId(Long categoryId);
    List<Counter> findByCategoryIdOrderByCodeAsc(Long categoryId);
    List<Counter> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Counter> findByCategoryIdAndActiveTrueAndStatus(Long categoryId, CounterStatus status);
    List<Counter> findByAssignedStaffEmailAndActiveTrue(String email);
    List<Counter> findByStatusAndActiveTrue(CounterStatus status);
    Optional<Counter> findByCode(String code);
    Optional<Counter> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
