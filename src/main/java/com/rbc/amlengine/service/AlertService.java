package com.rbc.amlengine.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for generating alerts based on transaction data.
 */
@Service
public class AlertService {

    private static final Logger logger = LogManager.getLogger(AlertService.class);

    /**
     * Generates an alert for the specified account when a threshold breach is detected.
     *
     * @param account the account that breached the threshold
     * @param currentSum the current sum of transactions
     * @param transactionTime the time of the transaction causing the breach
     */
    public void generateAlert(String account, double currentSum, String transactionTime) {
        logger.warn("ALERT: Account {} has breached the threshold with a current sum of {} at {}", account, currentSum, transactionTime);
        // Can extend here e.g., send notification, log to a database, etc.
    }
}