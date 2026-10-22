package org.lotus.edu;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe payment tracker that keeps track of the payment history for each card token.
 */
public class PaymentTracker {
    private final ConcurrentMap<String, Deque<Transaction>> history;

    public PaymentTracker() {
        this.history = new ConcurrentHashMap<>();
    }

    private record Transaction(long timestampMillis, double amount) { }

    public void registerPayment(String cardToken, long timestampMillis, double amount) {
        checkArgument(cardToken != null && !cardToken.isBlank(), "cardToken should not be blank");
        checkArgument(timestampMillis > 0, "timestampMillis should be greater than 0");
        checkArgument(amount > 0, "amount should be greater than or equal to 0");

        Transaction transaction = new Transaction(timestampMillis, amount);
        history.compute(cardToken, (_, deque) -> {
            if (deque == null) {
                deque = new ArrayDeque<>();
            }
            deque.addLast(transaction);
            return deque;
        });
    }

    public int getCardCountInLastXSeconds(String cardToken, long currentTimeMillis, int seconds) {
        checkArgument(cardToken != null && !cardToken.isBlank(), "cardToken should not be blank");
        checkArgument(currentTimeMillis > 0, "currentTimeMillis should be greater than 0");
        checkArgument(seconds > 0, "seconds should be greater than 0");

        final long durationInMillis = seconds * 1000L;
        final long cutoffTimeMillis = currentTimeMillis - durationInMillis;

        AtomicInteger count = new AtomicInteger(0);
        history.computeIfPresent(cardToken, (_, deque) -> {
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
        checkArgument(currentTimeMillis > 0, "currentTimeMillis should be greater than 0");
        checkArgument(seconds > 0, "seconds should be greater than 0");

        final long cutoffTimeMillis = currentTimeMillis - seconds * 1000L;
        for (String cardToken : history.keySet()) {
            history.computeIfPresent(cardToken, (_, deque) -> {
                deque.removeIf(t -> t.timestampMillis() < cutoffTimeMillis);
                return deque.isEmpty() ? null : deque;
            });
        }
    }

    private static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
