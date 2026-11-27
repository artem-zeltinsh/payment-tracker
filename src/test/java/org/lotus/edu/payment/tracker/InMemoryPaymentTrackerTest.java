package org.lotus.edu.payment.tracker;

import org.junit.jupiter.api.Test;
import org.lotus.edu.payment.Payment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.math.BigDecimal.*;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InMemoryPaymentTrackerTest {
    @Test
    public void whenPaymentIsNull_thenGetCardCountShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new InMemoryPaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCount(null, ofSeconds(10)));
    }

    @Test
    public void whenDurationIsNull_thenGetCardCountShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new InMemoryPaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCount(new Payment("cardId", Instant.now(), ONE), null));
    }

    @Test
    public void whenPaymentIsRegistered_thenGetCardCountShouldReturnCorrectCount() {
        PaymentTracker paymentTracker = new InMemoryPaymentTracker();
        Instant currentTime = Instant.now();

        paymentTracker.register(new Payment("1", currentTime.minusSeconds(1), TEN));
        paymentTracker.register(new Payment("1", currentTime.minusSeconds(55), TWO));
        paymentTracker.register(new Payment("1", currentTime.plusSeconds(1), ONE));
        paymentTracker.register(new Payment("2", currentTime, ONE));

        assertEquals(2,
                paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(55)));
        assertEquals(1,
                paymentTracker.getCardCount(new Payment("2", currentTime, TWO), ofSeconds(2)));
        assertEquals(0,
                paymentTracker.getCardCount(new Payment("3", currentTime, TWO), ofSeconds(2)));
    }

    @Test
    public void whenCutoffTimeIsNull_thenEvictOlderThanShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new InMemoryPaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.evictOlderThan(null));
    }

    @Test
    public void whenPaymentIsRegistered_thenEvictOlderThanShouldRemoveExpiredPayments() {
        PaymentTracker paymentTracker = new InMemoryPaymentTracker();

        Instant currentTime = Instant.now();

        paymentTracker.register(new Payment("1", currentTime.minusSeconds(1), ONE));
        paymentTracker.register(new Payment("1", currentTime.minusSeconds(600), TWO));
        paymentTracker.register(new Payment("1", currentTime.minusSeconds(3600), ONE));
        paymentTracker.register(new Payment("1", currentTime.minusSeconds(3601), TEN));

        paymentTracker.evictOlderThan(currentTime.minusSeconds(3600));
        assertEquals(3, paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(3600)));

        paymentTracker.evictOlderThan(currentTime.minusSeconds(3600));
        assertEquals(3, paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(3600)));

        paymentTracker.evictOlderThan(currentTime);
        assertEquals(0, paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(3600)));
    }

    @Test
    public void whenConcurrentPaymentsAreRegistered_thenAllRegistrationsAreCounted() throws Exception {
        PaymentTracker paymentTracker = new InMemoryPaymentTracker();

        int threadCount = 8;
        int paymentPerThread = 100;

        Instant currentTime = Instant.now();

        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> workers = new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {
                workers.add(pool.submit(() -> {
                    startSignal.await();
                    for (int j = 0; j < paymentPerThread; j++) {
                        Instant paymentTime = currentTime.minusMillis(1000 - j);
                        paymentTracker.register(new Payment("1", paymentTime, ONE));
                    }
                    return null;
                }));
            }

            startSignal.countDown();
            for (Future<?> worker : workers) {
                worker.get(1, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdown();
        }

        assertEquals(threadCount * paymentPerThread, paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(1)));
    }
}
