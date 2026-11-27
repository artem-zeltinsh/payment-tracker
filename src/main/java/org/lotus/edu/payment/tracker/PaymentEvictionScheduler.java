package org.lotus.edu.payment.tracker;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class PaymentEvictionScheduler implements AutoCloseable {
    private final PaymentTracker tracker;
    private final Duration retention;

    private final ScheduledExecutorService scheduler;

    public PaymentEvictionScheduler(PaymentTracker tracker, Duration retention, Duration interval) {
        checkArgument(tracker != null, "tracker should not be null");
        checkArgument(retention != null && retention.isPositive(), "retention should be positive");
        checkArgument(interval != null && interval.isPositive(), "interval should be positive");

        this.tracker = tracker;
        this.retention = retention;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "payment-eviction-thread");
            thread.setDaemon(true);
            return thread;
        });
        this.scheduler.scheduleAtFixedRate(this::evictExpired, interval.toMillis(), interval.toMillis(), MILLISECONDS);
    }

    private void evictExpired() {
        this.tracker.evictOlderThan(Instant.now().minus(retention));
    }

    @Override
    public void close() {
        this.scheduler.shutdown();
    }
}
