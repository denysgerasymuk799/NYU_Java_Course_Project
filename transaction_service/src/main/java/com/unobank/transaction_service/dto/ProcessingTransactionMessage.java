package com.unobank.transaction_service.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
public class ProcessingTransactionMessage {
    Timestamp createTimestamp;
    String eventName;
    String messageType;
    int responseType;
    String producer;
    String message;
    TransactionDto data;
    String status;

    public String getTransactionId() {
        return data.getTransactionId();
    }

    public String getSenderCardId() {
        return data.getSenderCardId();
    }

    public String getReceiverCardId() {
        return data.getReceiverCardId();
    }

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
