package com.smartqueue.service;

import com.smartqueue.dto.LiveCounterStatus;
import com.smartqueue.dto.LiveQueueDashboard;
import com.smartqueue.entity.Category;
import com.smartqueue.entity.Counter;
import com.smartqueue.entity.PriorityType;
import com.smartqueue.entity.Token;
import com.smartqueue.entity.TokenStatus;
import com.smartqueue.repository.CategoryRepository;
import com.smartqueue.repository.CounterRepository;
import com.smartqueue.repository.TokenRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiveQueueService {

    private final CategoryRepository categoryRepository;
    private final CounterRepository counterRepository;
    private final TokenRepository tokenRepository;
    private final AccessControlService accessControlService;

    public LiveQueueService(CategoryRepository categoryRepository,
                            CounterRepository counterRepository,
                            TokenRepository tokenRepository,
                            AccessControlService accessControlService) {
        this.categoryRepository = categoryRepository;
        this.counterRepository = counterRepository;
        this.tokenRepository = tokenRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional(readOnly = true)
    public LiveQueueDashboard dashboard(Long categoryId) {
        List<Category> categories = categoryRepository.findByActiveTrueOrderByNameAsc();
        return dashboard(categoryId, categories);
    }

    @Transactional(readOnly = true)
    public LiveQueueDashboard dashboardForAssignedCategory(Long assignedCategoryId) {
        List<Category> categories = categoryRepository.findById(assignedCategoryId)
                .filter(Category::isActive)
                .map(category -> List.of(category))
                .orElse(List.of());
        return dashboard(assignedCategoryId, categories);
    }

    @Transactional(readOnly = true)
    public LiveQueueDashboard dashboardForAssignedCategory(String email) {
        return dashboardForAssignedCategory(accessControlService.assignedCategoryId(email));
    }

    private LiveQueueDashboard dashboard(Long categoryId, List<Category> categories) {
        Category selectedCategory = selectedCategory(categoryId, categories);

        if (selectedCategory == null) {
            return new LiveQueueDashboard(categories, null, List.of(), List.of(), 0, 0);
        }

        List<Token> categoryWaitingTokens = activeWaitingQueue(selectedCategory.getId());
        List<LiveCounterStatus> counterStatuses = counterRepository.findByCategoryId(selectedCategory.getId()).stream()
                .map(counter -> counterStatus(counter, categoryWaitingTokens))
                .toList();

        return new LiveQueueDashboard(
                categories,
                selectedCategory,
                counterStatuses,
                categoryWaitingTokens.stream().limit(8).toList(),
                categoryWaitingTokens.size(),
                averageWait(categoryWaitingTokens)
        );
    }

    private Category selectedCategory(Long categoryId, List<Category> categories) {
        if (categories.isEmpty()) {
            return null;
        }
        if (categoryId == null) {
            return categories.getFirst();
        }
        return categories.stream()
                .filter(category -> category.getId().equals(categoryId))
                .findFirst()
                .orElse(categories.getFirst());
    }

    private LiveCounterStatus counterStatus(Counter counter, List<Token> categoryWaitingTokens) {
        List<Token> counterWaitingTokens = categoryWaitingTokens.stream()
                .filter(token -> token.getCounter() != null && token.getCounter().getId().equals(counter.getId()))
                .limit(5)
                .toList();

        return new LiveCounterStatus(
                counter,
                tokenRepository.findFirstByCounterIdAndStatusOrderByCalledAtDesc(counter.getId(), TokenStatus.CALLED),
                counterWaitingTokens,
                tokenRepository.countByCounterIdAndStatus(counter.getId(), TokenStatus.WAITING),
                averageWait(counterWaitingTokens)
        );
    }

    private List<Token> activeWaitingQueue(Long categoryId) {
        return tokenRepository.findByCategoryIdAndStatusOrderByIssuedAtAsc(categoryId, TokenStatus.WAITING).stream()
                .sorted(smartQueueComparator())
                .toList();
    }

    private Comparator<Token> smartQueueComparator() {
        return Comparator.comparingInt((Token token) -> priorityRank(token.getPriorityType()))
                .thenComparing(Token::getIssuedAt);
    }

    private int priorityRank(PriorityType priorityType) {
        return switch (priorityType) {
            case EMERGENCY -> 1;
            case SENIOR_CITIZEN -> 2;
            case VIP -> 3;
            case NORMAL -> 4;
        };
    }

    private double averageWait(List<Token> tokens) {
        return tokens.stream()
                .mapToInt(Token::getEstimatedWaitMinutes)
                .average()
                .orElse(0);
    }
}
