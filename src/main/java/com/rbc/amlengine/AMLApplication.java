package com.rbc.amlengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.rbc.amlengine.mapper.TransactionMapper;
import com.rbc.amlengine.model.Transaction;
import com.rbc.amlengine.service.TransactionProcessor;

@SpringBootApplication
public class AMLApplication implements CommandLineRunner {
    private final TransactionProcessor transactionProcessor;
    
    @Value("${csv.file.path}")
    private String csvFilePath;

    @Autowired
    public AMLApplication(TransactionProcessor transactionProcessor) {
        this.transactionProcessor = transactionProcessor;
    }

    public static void main(String[] args) {
        SpringApplication.run(AMLApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        processCSV(csvFilePath);
    }

    public void processCSV(String csvFilePath) {
        String line;
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new StringReader(getResourceFileAsString(csvFilePath)))) {
            while ((line = br.readLine()) != null) {
                String[] transactionData = line.split(cvsSplitBy);
                Transaction transaction = TransactionMapper.INSTANCE.toTransaction(line);
                transactionProcessor.processTransaction(transaction);
            }
        } catch (IOException e) {
            LogManager.getLogger(AMLApplication.class).error("Error reading the CSV file", e);
        }
    }
    
    private String getResourceFileAsString(String fileName) throws IOException {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (final InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalStateException("CSV " + fileName + " could not be loaded");
            }
            try (InputStreamReader isr = new InputStreamReader(inputStream);
                 final BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
}
