package com.rbc.amlengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("prod")
class ApplicationPropertiesProdTest {

    @Autowired
    private Environment env;

    @Test
    void testProdProfileProperties() {
        // Given: Active profile is prod
        // When: Retrieving properties
        double transactionThreshold = Double.parseDouble(env.getProperty("transaction.threshold"));
        int timeThresholdSeconds = Integer.parseInt(env.getProperty("time.threshold.seconds"));
        String csvFilePath = env.getProperty("csv.file.path");

        // Then: Properties match the prod profile settings
        assertEquals(50000.0, transactionThreshold, "Transaction threshold should be 50000.0 for prod profile");
        assertEquals(60, timeThresholdSeconds, "Time threshold seconds should be 60 for prod profile");
        assertEquals("prod-transactions.csv", csvFilePath, "CSV file path should be 'prod-transactions.csv' for prod profile");
    }
}
