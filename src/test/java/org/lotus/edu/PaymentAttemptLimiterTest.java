package org.lotus.edu;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentAttemptLimiterTest {

    @Test
    public void whenAccountIdIsNullOrBlank_thenIsAllowedShouldThrowIllegalArgumentException() {
        PaymentAttemptLimiter paymentAttemptLimiter = new PaymentAttemptLimiter(1000L, 3);
        assertThrows(IllegalArgumentException.class, () -> paymentAttemptLimiter.isAllowed(null));
        assertThrows(IllegalArgumentException.class, () -> paymentAttemptLimiter.isAllowed(" "));
    }

    @Test
    void whenAccountIdIsPresent_thenIsAllowedShouldReturnTrueUntilMaxAttemptsReached() throws InterruptedException {
        int threadCount = 20;
        String account = "acc101";

        PaymentAttemptLimiter paymentAttemptLimiter = new PaymentAttemptLimiter(1000L, 5);

        LongAdder successCount = new LongAdder();
        LongAdder failureCount = new LongAdder();

        CountDownLatch latch = new CountDownLatch(threadCount);

        Runnable checkPaymentTask = () -> {
            if (paymentAttemptLimiter.isAllowed(account)) {
                successCount.increment();
            } else {
                failureCount.increment();
            }
            latch.countDown();
        };

        IntStream.range(0, threadCount).forEach(_ -> new Thread(checkPaymentTask).start());
        latch.await();

        assertEquals(5, successCount.intValue());
        assertEquals(15, failureCount.intValue());
    }
}
