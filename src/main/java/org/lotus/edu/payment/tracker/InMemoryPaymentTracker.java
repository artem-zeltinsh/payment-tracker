package org.lotus.edu.payment.tracker;

import org.lotus.edu.payment.Payment;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Thread-safe payment tracker that keeps track of the payment history for each card token.
 */
public class InMemoryPaymentTracker implements PaymentTracker {
    private final ConcurrentMap<String, Deque<Payment>> history;

    public InMemoryPaymentTracker() {
        this.history = new ConcurrentHashMap<>();
    }

    @Override
    public void register(Payment payment) {
        checkArgument(payment != null, "payment should not be null");

        history.compute(payment.cardToken(), (_, cardHistory) -> {
            if (cardHistory == null) {
                cardHistory = new ArrayDeque<>();
            }
            cardHistory.addLast(payment);
            return cardHistory;
        });
    }

    @Override
    public int getCardCount(Payment payment, Duration duration) {
        checkArgument(payment != null, "payment should not be null");
        checkArgument(duration != null && duration.isPositive(), "duration should be positive");

        final Instant cutoffTime = payment.timestamp().minus(duration);

        final int[] countHolder = new int[1];
        history.computeIfPresent(payment.cardToken(), (_, cardHistory) -> {
            int count = 0;
            for (Payment prevPayment : cardHistory) {
                if (!prevPayment.timestamp().isBefore(cutoffTime) && !prevPayment.timestamp().isAfter(payment.timestamp())) {
                    count += 1;
                }
            }
            countHolder[0] = count;
            return cardHistory;
        });
        return countHolder[0];
    }

    @Override
    public void evictOlderThan(Instant cutoffTime) {
        checkArgument(cutoffTime != null, "cutoffTime should not be null");

        for (String cardToken : history.keySet()) {
            history.computeIfPresent(cardToken, (_, cardHistory) -> {
                cardHistory.removeIf(p -> p.timestamp().isBefore(cutoffTime));
                return cardHistory.isEmpty() ? null : cardHistory;
            });
        }
    }
}
