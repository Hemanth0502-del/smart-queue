package com.smartqueue.service;

import com.smartqueue.dto.CategoryTokenReport;
import com.smartqueue.dto.CounterServiceReport;
import com.smartqueue.dto.ReportsDashboard;
import com.smartqueue.entity.Category;
import com.smartqueue.entity.Counter;
import com.smartqueue.entity.Token;
import com.smartqueue.entity.TokenStatus;
import com.smartqueue.repository.CategoryRepository;
import com.smartqueue.repository.QueueLogRepository;
import com.smartqueue.repository.TokenRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportsService {

    private final TokenRepository tokenRepository;
    private final CategoryRepository categoryRepository;
    private final QueueLogRepository queueLogRepository;

    public ReportsService(TokenRepository tokenRepository,
                          CategoryRepository categoryRepository,
                          QueueLogRepository queueLogRepository) {
        this.tokenRepository = tokenRepository;
        this.categoryRepository = categoryRepository;
        this.queueLogRepository = queueLogRepository;
    }

    @Transactional(readOnly = true)
    public ReportsDashboard dashboard(LocalDate reportDate, Long categoryId) {
        LocalDate selectedDate = reportDate == null ? LocalDate.now() : reportDate;
        LocalDateTime start = selectedDate.atStartOfDay();
        LocalDateTime end = selectedDate.plusDays(1).atStartOfDay();

        List<Category> categories = categoryRepository.findAll();
        List<Token> issuedTokens = tokensIssuedBetween(start, end, categoryId);
        List<Token> filteredTokens = tokensForCategory(categoryId);

        List<Token> completedToday = filteredTokens.stream()
                .filter(token -> token.getCompletedAt() != null)
                .filter(token -> !token.getCompletedAt().isBefore(start) && token.getCompletedAt().isBefore(end))
                .toList();

        long skippedToday = categoryId == null
                ? queueLogRepository.countByActionAndActionTimeBetween("SKIPPED", start, end)
                : queueLogRepository.countActionForCategoryBetween("SKIPPED", start, end, categoryId);

        return new ReportsDashboard(
                selectedDate,
                categoryId,
                categories,
                issuedTokens.size(),
                completedToday.size(),
                skippedToday,
                waitingCount(categoryId),
                categoryTokenCounts(issuedTokens),
                counterServiceCounts(completedToday),
                issuedTokens
        );
    }

    private List<Token> tokensIssuedBetween(LocalDateTime start, LocalDateTime end, Long categoryId) {
        if (categoryId == null) {
            return tokenRepository.findByIssuedAtBetweenOrderByIssuedAtDesc(start, end);
        }
        return tokenRepository.findByCategoryIdAndIssuedAtBetweenOrderByIssuedAtDesc(categoryId, start, end);
    }

    private List<Token> tokensForCategory(Long categoryId) {
        if (categoryId == null) {
            return tokenRepository.findAll();
        }
        return tokenRepository.findByCategoryId(categoryId);
    }

    private long waitingCount(Long categoryId) {
        if (categoryId == null) {
            return tokenRepository.countByStatus(TokenStatus.WAITING);
        }
        return tokenRepository.countByCategoryIdAndStatus(categoryId, TokenStatus.WAITING);
    }

    private List<CategoryTokenReport> categoryTokenCounts(List<Token> issuedTokens) {
        Map<String, Long> counts = issuedTokens.stream()
                .collect(Collectors.groupingBy(
                        token -> token.getCategory().getName(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(entry -> new CategoryTokenReport(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategoryTokenReport::categoryName))
                .toList();
    }

    private List<CounterServiceReport> counterServiceCounts(List<Token> completedTokens) {
        Map<Counter, List<Token>> tokensByCounter = completedTokens.stream()
                .filter(token -> token.getCounter() != null)
                .collect(Collectors.groupingBy(Token::getCounter, LinkedHashMap::new, Collectors.toList()));

        return tokensByCounter.entrySet().stream()
                .map(entry -> toCounterReport(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CounterServiceReport::counterCode))
                .toList();
    }

    private CounterServiceReport toCounterReport(Counter counter, List<Token> completedTokens) {
        double averageMinutes = completedTokens.stream()
                .filter(token -> token.getCalledAt() != null && token.getCompletedAt() != null)
                .mapToLong(token -> Duration.between(token.getCalledAt(), token.getCompletedAt()).toMinutes())
                .average()
                .orElse(counter.getAverageServiceTimeInMinutes());

        return new CounterServiceReport(
                counter.getCode(),
                counter.getName(),
                counter.getCategory().getName(),
                completedTokens.size(),
                averageMinutes
        );
    }
}
