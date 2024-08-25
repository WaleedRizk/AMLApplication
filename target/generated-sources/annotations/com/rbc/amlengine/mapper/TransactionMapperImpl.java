package com.rbc.amlengine.mapper;

import com.rbc.amlengine.model.Transaction;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-08-25T13:52:44+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.33.0.v20230218-1114, environment: Java 17.0.7 (Oracle Corporation)"
)
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public Transaction toTransaction(String csvRow) {
        if ( csvRow == null ) {
            return null;
        }

        Transaction transaction = new Transaction();

        transaction.setTime( TransactionMapper.convertToTime( csvRow ) );
        transaction.setAmount( TransactionMapper.convertToAmount( csvRow ) );
        transaction.setAccount( TransactionMapper.convertToAccount( csvRow ) );

        return transaction;
    }
}
