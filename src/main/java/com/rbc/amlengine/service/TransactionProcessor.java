package com.rbc.amlengine.service;

import com.rbc.amlengine.model.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Service class for processing transactions and generating alerts based on transaction thresholds.
 */
@Service
public class TransactionProcessor {
    private static final Logger logger = LogManager.getLogger(TransactionProcessor.class);

    private final AlertService alertService;
    private final Map<String, Queue<Transaction>> accountTransactions = new HashMap<>();
    private final Map<String, Double> accountSums = new HashMap<>();

    @Value("${transaction.threshold}")
    private double alertThreshold;

    @Value("${time.threshold.seconds}")
    private int timeThresholdSeconds;

    /**
     * Constructs a TransactionProcessor with the specified AlertService.
     *
     * @param alertService the service used to generate alerts
     */
    @Autowired
    public TransactionProcessor(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Processes an incoming transaction. Adds the transaction to the account's queue, updates the sum of transactions
     * within the time threshold, and checks if the sum exceeds the alert threshold to generate an alert.
     *
     * @param incomingTransaction the transaction to process
     */
    public void processTransaction(Transaction incomingTransaction) {
        String account = incomingTransaction.getAccount();
        LocalTime transactionTime = incomingTransaction.getTime();
        double amount = incomingTransaction.getAmount();

        logger.info("Processing transaction for account: {}, time: {}, amount: {}", account, transactionTime, amount);

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
                logger.debug("Removed transaction outside time window: account={}, time={}, amount={}",
                        account, oldestTransaction.getTime(), oldestTransaction.getAmount());
            } else {
                break; // No need to check further if the oldest transaction is within the window
            }
        }

        // Add the new transaction to the queue and update the sum
        transactionsQueue.add(incomingTransaction);
        currentSum += amount;
        accountSums.put(account, currentSum);
        logger.debug("Updated sum for account {}: {}", account, currentSum);

        // Check if an alert needs to be generated
        if (currentSum > alertThreshold) {
            logger.warn("Threshold breached for account {}: current sum = {}", account, currentSum);
            alertService.generateAlert(account, currentSum, transactionTime.toString());
        }
    }

    /**
     * Resets the state of the TransactionProcessor, clearing all stored transactions and sums.
     * This method is primarily used for testing purposes.
     */
    public void reset() {
        accountTransactions.clear();
        accountSums.clear();
        logger.info("TransactionProcessor state has been reset.");
    }
}
