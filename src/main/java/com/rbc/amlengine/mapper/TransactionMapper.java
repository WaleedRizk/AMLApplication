package com.rbc.amlengine.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.rbc.amlengine.model.Transaction;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Mapper
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    // Map CSV rows to Transaction object with specific field conversion
    @Mapping(target = "time", source = "csvRow", qualifiedByName = "convertToTime" )
    @Mapping(target = "amount", source = "csvRow", qualifiedByName = "convertToAmount")
    @Mapping(target = "account", source = "csvRow", qualifiedByName = "convertToAccount")
    Transaction toTransaction(String csvRow); 

    // Method to convert the time from the CSV array element
    @Named("convertToTime")
    static LocalTime convertToTime(String csvRow) {
        if (csvRow.length() > 0) {
            return LocalTime.parse(csvRow.split(",")[0], DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        return null; // Handle cases where csvRow may be empty
    }

    // Method to convert the amount from the CSV array element
    @Named("convertToAmount")
    static double convertToAmount(String csvRow) {
        if (csvRow.length() > 1) {
            return Double.parseDouble(csvRow.split(",")[1]);
        }
        return 0.0; // Default or error handling
    }

    // Method to convert the account from the CSV array element
    @Named("convertToAccount")
    static String convertToAccount(String csvRow) {
        if (csvRow.length() > 2) {
            return csvRow.split(",")[2];
        }
        return ""; // Default or error handling
    }
}
