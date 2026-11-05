package org.lotus.edu;

import org.junit.jupiter.api.Test;
import org.lotus.edu.PaymentTracker.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.math.BigDecimal.*;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentTrackerTest {
    @Test
    public void whenCardIdIsEmpty_thenPaymentConstructorShouldThrowIllegalArgumentException() {
        Instant currentTime = Instant.now();

        assertThrows(IllegalArgumentException.class, () -> new Payment(null, currentTime, TEN));
        assertThrows(IllegalArgumentException.class, () -> new Payment(" ", currentTime, TEN));
    }

    @Test
    public void whenTimestampIsNull_thenPaymentConstructorShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Payment("cardId", null, TEN));
    }

    @Test
    public void whenAmountIsNegative_thenPaymentConstructorShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Payment("cardId", Instant.now(), BigDecimal.valueOf(-1)));
    }

    @Test
    public void whenPaymentIsNull_thenGetCardCountShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCount(null, ofSeconds(10)));
    }

    @Test
    public void whenDurationIsNull_thenGetCardCountShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCount(new Payment("cardId", Instant.now(), ONE), null));
    }

    @Test
    public void whenPaymentIsRegistered_thenGetCardCountShouldReturnCorrectCount() {
        PaymentTracker paymentTracker = new PaymentTracker();
        Instant currentTime = Instant.now();

        paymentTracker.registerPayment(new Payment("1", currentTime.minusSeconds(1), TEN));
        paymentTracker.registerPayment(new Payment("1", currentTime.minusSeconds(55), TWO));
        paymentTracker.registerPayment(new Payment("1", currentTime.plusSeconds(1), ONE));
        paymentTracker.registerPayment(new Payment("2", currentTime, ONE));

        assertEquals(2,
                paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(55)));
        assertEquals(1,
                paymentTracker.getCardCount(new Payment("2", currentTime, TWO), ofSeconds(2)));
        assertEquals(0,
                paymentTracker.getCardCount(new Payment("3", currentTime, TWO), ofSeconds(2)));
    }

    @Test
    public void whenCutoffTimeIsNull_thenEvictOlderThanShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.evictOlderThan(null));
    }

    @Test
    public void whenPaymentIsRegistered_thenEvictOlderThanShouldRemoveExpiredPayments() {
        PaymentTracker paymentTracker = new PaymentTracker();

        Instant currentTime = Instant.now();

        paymentTracker.registerPayment(new Payment("1", currentTime.minusSeconds(1), ONE));
        paymentTracker.registerPayment(new Payment("1", currentTime.minusSeconds(600), TWO));
        paymentTracker.registerPayment(new Payment("1", currentTime.minusSeconds(3600), ONE));
        paymentTracker.registerPayment(new Payment("1", currentTime.minusSeconds(3601), TEN));

        paymentTracker.evictOlderThan(currentTime.minusSeconds(3600));
        assertEquals(3, paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(3600)));

        paymentTracker.evictOlderThan(currentTime.minusSeconds(3600));
        assertEquals(3, paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(3600)));

        paymentTracker.evictOlderThan(currentTime);
        assertEquals(0, paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(3600)));
    }

    @Test
    public void whenConcurrentPaymentsAreRegistered_thenAllRegistrationsAreCounted() throws Exception {
        PaymentTracker paymentTracker = new PaymentTracker();

        int threadCount = 8;
        int paymentPerThread = 100;

        Instant currentTime = Instant.now();

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        try (ExecutorService pool = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    startSignal.await();
                    for (int j = 0; j < paymentPerThread; j++) {
                        Instant paymentTime = currentTime.minusMillis(1000 - j);
                        paymentTracker.registerPayment(new Payment("1", paymentTime, ONE));
                    }
                    doneSignal.countDown();
                    return null;
                });
            }

            startSignal.countDown();
            doneSignal.await();
        }

        assertEquals(threadCount * paymentPerThread, paymentTracker.getCardCount(new Payment("1", currentTime, ONE), ofSeconds(1)));
    }
}
