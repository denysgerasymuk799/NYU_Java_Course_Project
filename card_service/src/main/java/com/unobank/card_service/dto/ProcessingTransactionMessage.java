package com.unobank.card_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
