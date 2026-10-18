package org.lotus.edu;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentAttemptLimiter {
    private final long windowSizeMillis;
    private final int maxAllowedAttempts;

    private final ConcurrentHashMap<String, Deque<Long>> historyMap;

    public PaymentAttemptLimiter(long windowSizeMillis, int maxAllowedAttempts) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxAllowedAttempts = maxAllowedAttempts;
        this.historyMap = new ConcurrentHashMap<>();
    }

    public boolean isAllowed(String cardToken) {
        if (cardToken == null || cardToken.isBlank()) {
            throw new IllegalArgumentException("cardToken should not be null or blank");
        }

        long currentTimeMillis = System.currentTimeMillis();
        Deque<Long> timestamps = historyMap.computeIfAbsent(cardToken, _ -> new ArrayDeque<>());

        synchronized (timestamps) {
            long cutoffTime = currentTimeMillis - windowSizeMillis;

            while (!timestamps.isEmpty() && timestamps.peek() < cutoffTime) {
                timestamps.poll();
            }

            if (timestamps.size() >= maxAllowedAttempts) {
                return false;
            }

            timestamps.addLast(currentTimeMillis);
            return true;
        }
    }
}
