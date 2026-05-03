package com.smartqueue.service;

import com.smartqueue.dto.DashboardStats;
import com.smartqueue.entity.TokenStatus;
import com.smartqueue.repository.CounterRepository;
import com.smartqueue.repository.TokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final TokenRepository tokenRepository;
    private final CounterRepository counterRepository;
    private final AccessControlService accessControlService;

    public DashboardService(TokenRepository tokenRepository,
                            CounterRepository counterRepository,
                            AccessControlService accessControlService) {
        this.tokenRepository = tokenRepository;
        this.counterRepository = counterRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        return new DashboardStats(
                tokenRepository.count(),
                tokenRepository.countByStatus(TokenStatus.WAITING),
                tokenRepository.countByStatus(TokenStatus.CALLED),
                tokenRepository.countByStatus(TokenStatus.COMPLETED),
                counterRepository.countByActiveTrue()
        );
    }

    private DashboardStats getStatsForCategory(Long categoryId) {
        return new DashboardStats(
                tokenRepository.countByCategoryId(categoryId),
                tokenRepository.countByCategoryIdAndStatus(categoryId, TokenStatus.WAITING),
                tokenRepository.countByCategoryIdAndStatus(categoryId, TokenStatus.CALLED),
                tokenRepository.countByCategoryIdAndStatus(categoryId, TokenStatus.COMPLETED),
                counterRepository.countByCategoryIdAndActiveTrue(categoryId)
        );
    }

    @Transactional(readOnly = true)
    public DashboardStats getStatsForAssignedCategory(String email) {
        return getStatsForCategory(accessControlService.assignedCategoryId(email));
    }
}
