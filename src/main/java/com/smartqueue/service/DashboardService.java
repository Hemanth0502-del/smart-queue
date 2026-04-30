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

    public DashboardService(TokenRepository tokenRepository,
                            CounterRepository counterRepository) {
        this.tokenRepository = tokenRepository;
        this.counterRepository = counterRepository;
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
}
