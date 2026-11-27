package org.lotus.edu.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentTest {
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
}
