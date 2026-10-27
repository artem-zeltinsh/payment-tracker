package org.lotus.edu;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe payment tracker that keeps track of the payment history for each card token.
 */
public class PaymentTracker {
    private final ConcurrentMap<String, Deque<Payment>> history;

    public PaymentTracker() {
        this.history = new ConcurrentHashMap<>();
    }

    record Payment(String cardToken, Instant timestamp, double amount) {
        public Payment {
            checkArgument(cardToken != null && !cardToken.isBlank(), "cardToken should not be blank");
            checkArgument(timestamp != null, "timestamp should not be null");
            checkArgument(amount >= 0, "amount should be greater than or equal to 0");
        }
    }

    public void registerPayment(Payment payment) {
        checkArgument(payment != null, "payment should not be null");

        history.compute(payment.cardToken, (_, cardHistory) -> {
            if (cardHistory == null) {
                cardHistory = new ArrayDeque<>();
            }
            cardHistory.addLast(payment);
            return cardHistory;
        });
    }

    public int getCardCount(Payment payment, Duration duration) {
        checkArgument(payment != null, "payment should not be null");
        checkArgument(duration != null, "duration should not be null");

        final long durationInMillis = duration.toMillis();
        final long cutoffTimeMillis = payment.timestamp.toEpochMilli() - durationInMillis;

        final int[] countHolder = new int[1];
        history.computeIfPresent(payment.cardToken, (_, cardHistory) -> {
            int count = 0;
            for (Payment prevPayment : cardHistory) {
                if (prevPayment.timestamp.toEpochMilli() >= cutoffTimeMillis
                        && prevPayment.timestamp.toEpochMilli() <= payment.timestamp.toEpochMilli()) {
                    count += 1;
                }
            }
            countHolder[0] = count;
            return cardHistory;
        });
        return countHolder[0];
    }

    public void evictOlderThan(Duration duration) {
        checkArgument(duration != null, "duration should not be null");

        final long currentTimeMillis = Instant.now().toEpochMilli();
        final long cutoffTimeMillis = currentTimeMillis - duration.toMillis();
        for (String cardToken : history.keySet()) {
            history.computeIfPresent(cardToken, (_, cardHistory) -> {
                cardHistory.removeIf(p -> p.timestamp.toEpochMilli() < cutoffTimeMillis);
                return cardHistory.isEmpty() ? null : cardHistory;
            });
        }
    }

    private static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
