package org.lotus.edu;

import org.junit.jupiter.api.Test;
import org.lotus.edu.PaymentTracker.Payment;

import java.time.Duration;
import java.time.Instant;

import static java.math.BigDecimal.ONE;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentEvictionSchedulerTest {
    @Test
    public void testPaymentEvictionScheduler() throws Exception {
        Instant currentTime = Instant.now();

        PaymentTracker paymentTracker = new PaymentTracker();
        paymentTracker.register(new Payment("card-1", currentTime.minusSeconds(1), ONE));
        paymentTracker.register(new Payment("card-1", currentTime, ONE));

        Instant testExpirationTime = Instant.now().plusSeconds(1);
        try (PaymentEvictionScheduler _ = new PaymentEvictionScheduler(paymentTracker, ofSeconds(1), ofMillis(100))) {
            int count = 0;
            do {
                Thread.sleep(100);
                Payment testQuery = new Payment("card-1", Instant.now(), ONE);
                count = paymentTracker.getCardCount(testQuery, Duration.ofSeconds(10));
            } while (count == 2 && Instant.now().isBefore(testExpirationTime));

            assertEquals(1, count, "Payment count should be 1 after eviction");
        }
    }
}
