package org.lotus.edu.payment;

import java.math.BigDecimal;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkArgument;

public record Payment(String cardToken, Instant timestamp, BigDecimal amount) {
    public Payment {
        checkArgument(cardToken != null && !cardToken.isBlank(), "cardToken should not be blank");
        checkArgument(timestamp != null, "timestamp should not be null");
        checkArgument(amount != null && amount.compareTo(BigDecimal.ZERO) >= 0, "amount should be greater than or equal to 0");
    }
}
