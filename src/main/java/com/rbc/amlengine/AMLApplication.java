package com.rbc.amlengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.rbc.amlengine.mapper.TransactionMapper;
import com.rbc.amlengine.model.Transaction;
import com.rbc.amlengine.service.TransactionProcessor;

/**
 * Main application class for the AML engine. This class processes transactions
 * from a CSV file and checks if they breach the specified threshold.
 */
@SpringBootApplication
public class AMLApplication implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(AMLApplication.class);

    private final TransactionProcessor transactionProcessor;

    @Value("${csv.file.path}")
    private String csvFilePath;

    /**
     * Constructs an AMLApplication with the specified TransactionProcessor.
     *
     * @param transactionProcessor the processor to handle transactions
     */
    @Autowired
    public AMLApplication(TransactionProcessor transactionProcessor) {
        this.transactionProcessor = transactionProcessor;
    }

    /**
     * The main entry point of the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AMLApplication.class, args);
    }

    /**
     * Runs the application, processing transactions from the specified CSV file.
     *
     * @param args the command line arguments
     */
    @Override
    public void run(String... args) {
        logger.info("Starting AMLApplication with CSV file path: {}", csvFilePath);
        processCSV(csvFilePath);
    }

    /**
     * Processes the transactions in the specified CSV file.
     *
     * @param csvFilePath the path to the CSV file containing transactions
     */
    public void processCSV(String csvFilePath) {
        logger.info("Processing CSV file: {}", csvFilePath);
        String line;

        try (BufferedReader br = new BufferedReader(new StringReader(getResourceFileAsString(csvFilePath)))) {
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                logger.debug("Reading line {}: {}", lineNumber, line);
                Transaction transaction = TransactionMapper.INSTANCE.toTransaction(line);
                transactionProcessor.processTransaction(transaction);
            }
        } catch (IOException e) {
            logger.error("Error reading the CSV file: {}", csvFilePath, e);
        }
    }

    /**
     * Reads the contents of a resource file as a String.
     *
     * @param fileName the name of the resource file
     * @return the contents of the file as a String
     * @throws IOException if an error occurs while reading the file
     */
    private String getResourceFileAsString(String fileName) throws IOException {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                logger.error("CSV file not found: {}", fileName);
                throw new IllegalStateException("CSV " + fileName + " could not be loaded");
            }
            try (InputStreamReader isr = new InputStreamReader(inputStream);
                 final BufferedReader reader = new BufferedReader(isr)) {
                logger.debug("Successfully loaded CSV file: {}", fileName);
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
}
