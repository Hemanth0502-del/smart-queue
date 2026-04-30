package com.smartqueue.repository;

import com.smartqueue.entity.QueueLog;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueueLogRepository extends JpaRepository<QueueLog, Long> {
    List<QueueLog> findByTokenIdOrderByActionTimeDesc(Long tokenId);
    long countByActionAndActionTimeBetween(String action, LocalDateTime start, LocalDateTime end);

    @Query("""
            select count(log) from QueueLog log
            where log.action = :action
              and log.actionTime >= :start
              and log.actionTime < :end
              and log.token.category.id = :categoryId
            """)
    long countActionForCategoryBetween(@Param("action") String action,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("categoryId") Long categoryId);
}
