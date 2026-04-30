package com.smartqueue.dto;

import com.smartqueue.entity.Counter;
import com.smartqueue.entity.Token;
import java.util.Optional;

public record CounterQueueStatus(
        Counter counter,
        Optional<Token> calledToken,
        Optional<Token> nextWaitingToken,
        long waitingCount
) {
}
