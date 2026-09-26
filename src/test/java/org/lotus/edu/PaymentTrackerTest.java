package org.lotus.edu;

import org.junit.jupiter.api.Test;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentTrackerTest {
    @Test
    public void whenCardIdIsEmpty_thenRegisterPaymentShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        long currentTimeMillis = currentTimeMillis();

        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.registerPayment(null, currentTimeMillis, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.registerPayment("", currentTimeMillis, 0.0));
    }

    @Test
    public void whenTimestampIsNegative_thenRegisterPaymentShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();

        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.registerPayment("cardId", -1, 0.0));
    }

    @Test
    public void whenAmountIsNegative_thenRegisterPaymentShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        long currentTimeMillis = currentTimeMillis();

        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.registerPayment("cardId", currentTimeMillis, -1.0));
    }

    @Test
    public void whenCardIdIsEmpty_thenGetCardCountInLastXSecondsShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        long currentTimeMillis = currentTimeMillis();

        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCountInLastXSeconds(null, currentTimeMillis, 10));
        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCountInLastXSeconds("", currentTimeMillis, 10));
    }

    @Test
    public void whenSecondsIsNegative_thenGetCardCountInLastXSecondsShouldThrowIllegalArgumentException() {
        PaymentTracker paymentTracker = new PaymentTracker();
        long currentTimeMillis = currentTimeMillis();

        assertThrows(IllegalArgumentException.class,
                () -> paymentTracker.getCardCountInLastXSeconds("cardId", currentTimeMillis, 0));
    }

    @Test
    public void whenPaymentIsRegistered_thenGetCardCountInLastXSecondsShouldReturnCorrectCount() {
        PaymentTracker paymentTracker = new PaymentTracker();
        long currentTimeMillis = currentTimeMillis();

        paymentTracker.registerPayment("1", currentTimeMillis, 10.0);
        paymentTracker.registerPayment("1", currentTimeMillis, 2.0);
        paymentTracker.registerPayment("2", currentTimeMillis, 1.5);

        assertEquals(2, paymentTracker.getCardCountInLastXSeconds("1", currentTimeMillis, 1));
        assertEquals(1, paymentTracker.getCardCountInLastXSeconds("2", currentTimeMillis,2));
        assertEquals(0, paymentTracker.getCardCountInLastXSeconds("3", currentTimeMillis, 2));
    }
}
