package org.lotus.edu;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentAttemptLimiter {
    private final long windowSizeMillis;
    private final int maxAllowedAttempts;

    private final ConcurrentHashMap<String, Deque> historyMap;

    public PaymentAttemptLimiter(long windowSizeMillis, int maxAllowedAttempts) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxAllowedAttempts = maxAllowedAttempts;
        this.historyMap = new ConcurrentHashMap<>();
    }

    public boolean isAllowed(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId is null or blank");
        }

        long currentTimeMillis = System.currentTimeMillis();
        Deque<Long> timestamps = historyMap.computeIfAbsent(accountId, _ -> new ArrayDeque());

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
