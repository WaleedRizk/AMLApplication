package com.rbc.amlengine.service;

import com.rbc.amlengine.AMLApplication;
import com.rbc.amlengine.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = AMLApplication.class)
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "csv.file.path=ignored.csv" // Set to an ignored path for the test
})
class TransactionProcessorTest {

    @MockBean
    private AlertService alertService; // Mock AlertService to isolate testing

    @MockBean
    private AMLApplication amlApplication; // Mock the entire AMLApplication to prevent file reading

    @Autowired
    private TransactionProcessor transactionProcessor;

    @Value("${transaction.threshold}")
    private double alertThreshold;

    @Value("${time.threshold.seconds}")
    private int timeThresholdSeconds;

    @BeforeEach
    void setUp() {
        // Reset the state of the transaction processor
        transactionProcessor.reset();

        // Prevent AMLApplication from processing CSV
        doNothing().when(amlApplication).processCSV(anyString());

        // Verify that the properties are loaded correctly in the Spring context
        System.out.println("Alert Threshold (from properties): " + alertThreshold);
        System.out.println("Time Threshold Seconds (from properties): " + timeThresholdSeconds);

        // Assert to make sure properties are loaded correctly
        assertEquals(50000.0, alertThreshold, "Alert threshold should match the dev profile configuration.");
        assertEquals(60, timeThresholdSeconds, "Time threshold should match the dev profile configuration.");
    }

    @Test
    void testProcessTransactionWithoutAlert() {
        // Given: A transaction that does not exceed the threshold
        Transaction transaction1 = new Transaction(LocalTime.of(10, 10, 10), 10000, "1");
        Transaction transaction2 = new Transaction(LocalTime.of(10, 10, 20), 20000, "1");

        // When: Processing transactions that don't exceed the threshold
        transactionProcessor.processTransaction(transaction1);
        transactionProcessor.processTransaction(transaction2);

        // Then: No alert should be generated
        verify(alertService, never()).generateAlert(anyString(), anyDouble(), anyString());
    }

    @Test
    void testProcessTransactionWithAlert() {
        // Given: Transactions that exceed the threshold within the time window
        Transaction transaction1 = new Transaction(LocalTime.of(10, 10, 10), 30000, "1");
        Transaction transaction2 = new Transaction(LocalTime.of(10, 10, 20), 25000, "1");

        // When: Processing transactions that exceed the threshold
        transactionProcessor.processTransaction(transaction1);
        transactionProcessor.processTransaction(transaction2);

        // Then: An alert should be generated
        verify(alertService, times(1)).generateAlert(eq("1"), eq(55000.0), anyString());
    }

    @Test
    void testProcessTransactionWithMultipleAlerts() {
        // Given: Multiple sets of transactions that exceed the threshold
        Transaction transaction1 = new Transaction(LocalTime.of(10, 10, 10), 25000, "1");
        Transaction transaction2 = new Transaction(LocalTime.of(10, 10, 20), 30000, "1");
        Transaction transaction3 = new Transaction(LocalTime.of(10, 11, 00), 50000, "1");
        Transaction transaction4 = new Transaction(LocalTime.of(10, 11, 30), 10000, "1");
        Transaction transaction5 = new Transaction(LocalTime.of(10, 12, 30), 10000, "1");
        Transaction transaction6 = new Transaction(LocalTime.of(10, 12, 45), 10000, "1");

        // When: Processing multiple transactions that exceed the threshold
        transactionProcessor.processTransaction(transaction1);
        transactionProcessor.processTransaction(transaction2);
        transactionProcessor.processTransaction(transaction3);
        transactionProcessor.processTransaction(transaction4);
        transactionProcessor.processTransaction(transaction5);
        transactionProcessor.processTransaction(transaction6);

        // Then: Multiple alerts should be generated
        verify(alertService, times(3)).generateAlert(eq("1"), anyDouble(), anyString());
    }
}
