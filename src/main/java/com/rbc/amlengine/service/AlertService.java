package com.rbc.amlengine.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    private static final Logger logger = LogManager.getLogger(AlertService.class);

    public void generateAlert(String account, double total, String time) {
        logger.warn("Alert: Account {} has exceeded the threshold with transactions totalling ${} at time {}", account, total, time);
    }
}
