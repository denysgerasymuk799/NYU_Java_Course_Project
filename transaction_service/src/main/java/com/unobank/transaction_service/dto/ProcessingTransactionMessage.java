package com.unobank.transaction_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessingTransactionMessage {
    String eventName;
    String messageType;
    int responseType;
    String producer;
    String message;
    TransactionDto data;

    public ProcessingTransactionMessage() {}

    public ProcessingTransactionMessage(String eventName, String messageType, int responseType, String producer,
                                        String message, TransactionDto transactionDto) {
        this.eventName = eventName;
        this.messageType = messageType;
        this.responseType = responseType;
        this.producer = producer;
        this.message = message;
        this.data = transactionDto;
    }
}
