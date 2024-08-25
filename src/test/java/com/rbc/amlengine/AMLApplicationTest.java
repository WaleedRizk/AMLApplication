package com.rbc.amlengine;

import com.rbc.amlengine.model.Transaction;
import com.rbc.amlengine.service.TransactionProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.FileWriter;
import java.io.IOException;

import static org.mockito.Mockito.*;

@SpringBootTest // Load the full application context
@ActiveProfiles("prod")  // Use the 'prod' profile for this test
@TestPropertySource(properties = {
    "csv.file.path=prod-transactions.csv" // Override CSV file path to a dummy path for testing
})
class AMLApplicationTest {

    @MockBean
    private TransactionProcessor transactionProcessor; // Mock TransactionProcessor to prevent actual processing

    @Autowired
    private AMLApplication amlApplication; // Autowire the actual AMLApplication bean

    private static final String TEST_CSV_PATH = "prod-transactions.csv"; // Path for temporary test file

    @BeforeEach
    void setUp() throws IOException {
        // Prevent actual CSV processing by mocking the method call
        doNothing().when(transactionProcessor).processTransaction(any(Transaction.class));

    }

    @Test
    void testProcessCSV() throws IOException {
        // Given: A valid CSV file path
        amlApplication.processCSV(TEST_CSV_PATH);

        // Then: Verify that the processTransaction method is called the expected number of times
        verify(transactionProcessor, times(30)).processTransaction(any(Transaction.class));
    }
}
