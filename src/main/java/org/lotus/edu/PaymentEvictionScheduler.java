package org.lotus.edu;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class PaymentEvictionScheduler implements AutoCloseable {
    private final PaymentTracker tracker;
    private final ScheduledExecutorService scheduler;
    private final Duration retention;

    public PaymentEvictionScheduler(PaymentTracker tracker, Duration retention, Duration interval) {
        this.tracker = tracker;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "payment-eviction-thread");
            thread.setDaemon(true);
            return thread;
        });
        this.retention = retention;
        this.scheduler.scheduleAtFixedRate(this::evictExpired, interval.toMillis(), interval.toMillis(), MILLISECONDS);
    }

    private void evictExpired() {
        tracker.evictOlderThan(Instant.now().minus(retention));
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}
