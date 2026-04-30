package com.smartqueue.dto;

import com.smartqueue.entity.Counter;
import com.smartqueue.entity.Token;
import java.util.List;
import java.util.Optional;

public record LiveCounterStatus(
        Counter counter,
        Optional<Token> currentServingToken,
        List<Token> nextWaitingTokens,
        long waitingCount,
        double averageEstimatedWaitMinutes
) {
}
