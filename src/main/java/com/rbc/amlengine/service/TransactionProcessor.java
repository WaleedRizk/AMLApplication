package com.rbc.amlengine.service;

import com.rbc.amlengine.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@Service
public class TransactionProcessor {
    private final AlertService alertService;
    private final Map<String, Queue<Transaction>> accountTransactions = new HashMap<>();
    private final Map<String, Double> accountSums = new HashMap<>();

    @Value("${transaction.threshold}")
    private double alertThreshold;

    @Value("${time.threshold.seconds}")
    private int timeThresholdSeconds;

    @Autowired
    public TransactionProcessor(AlertService alertService) {
        this.alertService = alertService;
    }

    public void processTransaction(Transaction incomingTransaction) {
        String account = incomingTransaction.getAccount();
        LocalTime transactionTime = incomingTransaction.getTime();
        double amount = incomingTransaction.getAmount();

        // Initialize the account data if it doesn't exist
        accountTransactions.putIfAbsent(account, new ArrayDeque<>());
        accountSums.putIfAbsent(account, 0.0);

        Queue<Transaction> transactionsQueue = accountTransactions.get(account);
        double currentSum = accountSums.get(account);

        // Remove transactions outside the configured time window and update the current sum
        while (!transactionsQueue.isEmpty()) {
            Transaction oldestTransaction = transactionsQueue.peek();
            if (oldestTransaction.getTime().isBefore(transactionTime.minusSeconds(timeThresholdSeconds))) {
                currentSum -= oldestTransaction.getAmount();
                transactionsQueue.poll();
            } else {
                break; // No need to check further if the oldest transaction is within the window
            }
        }

        // Add the new transaction to the queue and update the sum
        transactionsQueue.add(incomingTransaction);
        currentSum += amount;
        accountSums.put(account, currentSum);

        // Check if an alert needs to be generated
        if (currentSum > alertThreshold) {
            alertService.generateAlert(account, currentSum, transactionTime.toString());
        }
    }

    // Method to reset state for testing purposes
    public void reset() {
        accountTransactions.clear();
        accountSums.clear();
    }
}
