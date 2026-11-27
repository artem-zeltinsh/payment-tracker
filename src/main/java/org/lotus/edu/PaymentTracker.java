package org.lotus.edu;

import java.time.Duration;
import java.time.Instant;

public interface PaymentTracker {

    /**
     * Registers the specified payment in the tracker so it can be included in later card activity counts.
     *
     * @param payment the payment to register; must not be null
     */
    void register(Payment payment);

    /**
     * Returns the number of registered payments for the same card token
     * whose timestamps fall within the time window ending at the payment timestamp.
     *
     * @param payment the payment that provides the card token and end timestamp for the count; must not be null
     * @param duration the positive duration before the payment timestamp to include in the count; must not be null
     * @return the number of registered payments for the same card token within the specified duration
     */
    int getCardCount(Payment payment, Duration duration);

    /**
     * Evicts registered payments whose timestamps are before the specified cutoff time.
     *
     * @param cutoffTime the cutoff time used to determine which payments to evict; must not be null
     */
    void evictOlderThan(Instant cutoffTime);
}
