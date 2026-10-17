package org.lotus.edu;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class PaymentTracker {
    private final ConcurrentMap<String, Queue<Transaction>> history;

    public PaymentTracker() {
        this.history = new ConcurrentHashMap<>();
    }

    private record Transaction(long timestampMillis, double amount) { }

    public void registerPayment(String cardId, long timestampMillis, double amount) {
        if (cardId == null || cardId.isBlank()) {
            throw new IllegalArgumentException("cardId should not be blank");
        }
        if (timestampMillis <= 0) {
            throw new IllegalArgumentException("timestampMillis should be greater than 0");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("amount should be greater than or equal to 0");
        }

        Queue<Transaction> transactions = history.computeIfAbsent(cardId, _ -> new ConcurrentLinkedQueue<>());

        Transaction transaction = new Transaction(timestampMillis, amount);
        transactions.add(transaction);
    }

    public int getCardCountInLastXSeconds(String cardId, long currentTimeMillis, int seconds) {
        if (cardId == null || cardId.isBlank()) {
            throw new IllegalArgumentException("cardId should not be blank");
        }
        if (currentTimeMillis <= 0) {
            throw new IllegalArgumentException("currentTimeMillis should be greater than 0");
        }
        if (seconds <= 0) {
            throw new IllegalArgumentException("seconds should be greater than 0");
        }

        Queue<Transaction> transactionsByCard = history.get(cardId);
        if (transactionsByCard == null) {
            return 0;
        }

        final long durationInMillis = seconds * 1000L;
        final long cutoffTime = currentTimeMillis - durationInMillis;

        synchronized (transactionsByCard) {
            while (!transactionsByCard.isEmpty() && transactionsByCard.peek().timestampMillis < cutoffTime) {
                transactionsByCard.poll();
            }
            return transactionsByCard.size();
        }
    }
}
