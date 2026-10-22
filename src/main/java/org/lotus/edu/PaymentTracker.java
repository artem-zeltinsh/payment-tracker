package org.lotus.edu;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PaymentTracker {
    private final ConcurrentMap<String, Deque<Transaction>> history;

    public PaymentTracker() {
        this.history = new ConcurrentHashMap<>();
    }

    private record Transaction(long timestampMillis, double amount) { }

    public void registerPayment(String cardId, long timestampMillis, double amount) {
        requireNotBlank(cardId, "cardId should not be blank");
        requirePositive(timestampMillis, "timestampMillis should be greater than 0");
        requireNotNegative(amount, "amount should be greater than or equal to 0");

        Transaction transaction = new Transaction(timestampMillis, amount);
        history.compute(cardId, (_, deque) -> {
            if (deque == null) {
                deque = new ArrayDeque<>();
            }
            deque.addLast(transaction);
            return deque;
        });
    }

    public int getCardCountInLastXSeconds(String cardId, long currentTimeMillis, int seconds) {
        requireNotBlank(cardId, "cardId should not be blank");
        requirePositive(currentTimeMillis, "currentTimeMillis should be greater than 0");
        requirePositive(seconds, "seconds should be greater than 0");

        final long durationInMillis = seconds * 1000L;
        final long cutoffTimeMillis = currentTimeMillis - durationInMillis;

        AtomicInteger count = new AtomicInteger(0);
        history.computeIfPresent(cardId, (_, deque) -> {
            for (Transaction transaction : deque) {
                if (transaction.timestampMillis() >= cutoffTimeMillis && transaction.timestampMillis() <= currentTimeMillis) {
                    count.incrementAndGet();
                }
            }
            return deque;
        });
        return count.get();
    }

    public void evictOlderThanXSeconds(long currentTimeMillis, long seconds) {
        requirePositive(currentTimeMillis, "currentTimeMillis should be greater than 0");
        requirePositive(seconds, "seconds should be greater than 0");

        final long cutoffTimeMillis = currentTimeMillis - seconds * 1000L;
        for (String cardId : history.keySet()) {
            history.computeIfPresent(cardId, (_, deque) -> {
                deque.removeIf(t -> t.timestampMillis() < cutoffTimeMillis);
                return deque.isEmpty() ? null : deque;
            });
        }
    }

    private static void requireNotBlank(String param, String message) {
        if (param == null || param.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requirePositive(long param, String message) {
        if (param <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requireNotNegative(double param, String message) {
        if (param < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
