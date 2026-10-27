package org.lotus.edu;

import org.junit.jupiter.api.Test;
import org.lotus.edu.PaymentTracker.Payment;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentTrackerTest {
    @Test
    public void whenCardIdIsEmpty_thenPaymentConstructorShouldThrowIllegalArgumentException() {
        Instant currentTime = Instant.now();

        assertThrows(IllegalArgumentException.class, () -> new Payment(null, currentTime, 5.0));
        assertThrows(IllegalArgumentException.class, () -> new Payment(" ", currentTime, 10.0));
    }

    @Test
    public void whenTimestampIsNull_thenPaymentConstructorShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Payment("cardId", null, 10));
    }

    @Test
    public void whenAmountIsNegative_thenPaymentConstructorShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Payment("cardId", Instant.now(), -1.0));
    }

    @Test
    public void whenPaymentIsNull_thenGetCardCountInLastXSecondsShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCount(null, Duration.ofSeconds(10)));
    }

    @Test
    public void whenDurationIsNull_thenGetCardCountInLastXSecondsShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCount(new Payment("cardId", Instant.now(), 1), null));
    }

    @Test
    public void whenPaymentIsRegistered_thenGetCardCountInLastXSecondsShouldReturnCorrectCount() {
        PaymentTracker paymentTracker = new PaymentTracker();
        Instant currentTime = Instant.now();

        paymentTracker.registerPayment(new Payment("1", currentTime, 10.0));
        paymentTracker.registerPayment(new Payment("1", currentTime, 2.0));
        paymentTracker.registerPayment(new Payment("2", currentTime, 1.5));

        assertEquals(2, paymentTracker.getCardCount(new Payment("1", currentTime, 1), Duration.ofSeconds(2)));
        assertEquals(1, paymentTracker.getCardCount(new Payment("2", currentTime,2), Duration.ofSeconds(2)));
        assertEquals(0, paymentTracker.getCardCount(new Payment("3", currentTime, 2), Duration.ofSeconds(2)));
    }

    @Test
    public void whenSecondsIsNegative_thenEvictOlderThanXSecondsShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.evictOlderThan(null));
    }
}
