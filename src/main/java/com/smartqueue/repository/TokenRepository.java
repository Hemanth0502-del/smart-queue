package com.smartqueue.repository;

import com.smartqueue.entity.Token;
import com.smartqueue.entity.TokenStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenNumber(String tokenNumber);
    long countByStatus(TokenStatus status);
    long countByStatusAndIssuedAtBetween(TokenStatus status, LocalDateTime start, LocalDateTime end);
    long countByCategoryIdAndStatus(Long categoryId, TokenStatus status);
    long countByCategoryIdAndStatusAndIssuedAtBetween(Long categoryId, TokenStatus status, LocalDateTime start, LocalDateTime end);
    long countByCategoryIdAndIssuedAtBetween(Long categoryId, LocalDateTime start, LocalDateTime end);
    List<Token> findByIssuedAtBetweenOrderByIssuedAtDesc(LocalDateTime start, LocalDateTime end);
    List<Token> findByCategoryIdAndIssuedAtBetweenOrderByIssuedAtDesc(Long categoryId, LocalDateTime start, LocalDateTime end);
    boolean existsByUserEmailAndCategoryIdAndStatusIn(String email, Long categoryId, List<TokenStatus> statuses);
    Optional<Token> findFirstByUserEmailAndStatusInOrderByIssuedAtDesc(String email, List<TokenStatus> statuses);
    List<Token> findByStatusOrderByIssuedAtAsc(TokenStatus status);
    List<Token> findByUserEmailOrderByIssuedAtDesc(String email);
    List<Token> findByCategoryId(Long categoryId);
    List<Token> findByCategoryIdAndStatus(Long categoryId, TokenStatus status);
    List<Token> findByCounterIdAndStatus(Long counterId, TokenStatus status);
    List<Token> findByCategoryIdAndStatusOrderByIssuedAtAsc(Long categoryId, TokenStatus status);
    Optional<Token> findFirstByCategoryIdAndStatusOrderByIssuedAtAsc(Long categoryId, TokenStatus status);
    Optional<Token> findFirstByCounterIdAndStatusOrderByCalledAtDesc(Long counterId, TokenStatus status);
    long countByCounterIdAndStatus(Long counterId, TokenStatus status);
    List<Token> findAllByOrderByIssuedAtDesc();
    List<Token> findTop10ByOrderByIssuedAtDesc();
}
