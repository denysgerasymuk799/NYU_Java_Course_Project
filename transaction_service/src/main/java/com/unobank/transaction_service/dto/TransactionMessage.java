package com.unobank.transaction_service.dto;

import lombok.Data;

@Data
public class TransactionMessage {
    String eventName;
    String messageType;
    int responseType;
    String producer;
    String message;
    TransactionInfo data;

    public String getTransactionId() {
        return data.getTransactionId();
    }

    public String getSenderCardId() {
        return data.getSenderCardId();
    }

    public String getReceiverCardId() {
        return data.getReceiverCardId();
    }
}
