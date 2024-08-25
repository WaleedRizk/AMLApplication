package com.rbc.amlengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Class to hold all transaction data.
 * @author waleed
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private LocalTime time;
    private double amount;
    private String account;
}
