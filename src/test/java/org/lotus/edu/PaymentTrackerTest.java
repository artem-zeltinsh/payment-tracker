package org.lotus.edu;

import org.junit.jupiter.api.Test;
import org.lotus.edu.PaymentTracker.Payment;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static java.math.BigDecimal.*;
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
    public void whenPaymentIsNull_thenGetCardCountInLastXSecondsShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCount(null, Duration.ofSeconds(10)));
    }

    @Test
    public void whenDurationIsNull_thenGetCardCountInLastXSecondsShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCount(new Payment("cardId", Instant.now(), ONE), null));
    }

    @Test
    public void whenPaymentIsRegistered_thenGetCardCountInLastXSecondsShouldReturnCorrectCount() {
        PaymentTracker paymentTracker = new PaymentTracker();
        Instant currentTime = Instant.now();

        paymentTracker.registerPayment(new Payment("1", currentTime, TEN));
        paymentTracker.registerPayment(new Payment("1", currentTime, TWO));
        paymentTracker.registerPayment(new Payment("2", currentTime, ONE));

        assertEquals(2,
                paymentTracker.getCardCount(new Payment("1", currentTime, ONE), Duration.ofSeconds(2)));
        assertEquals(1,
                paymentTracker.getCardCount(new Payment("2", currentTime, TWO), Duration.ofSeconds(2)));
        assertEquals(0,
                paymentTracker.getCardCount(new Payment("3", currentTime, TWO), Duration.ofSeconds(2)));
    }

    @Test
    public void whenSecondsIsNegative_thenEvictOlderThanXSecondsShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.evictOlderThan(null));
    }
}
