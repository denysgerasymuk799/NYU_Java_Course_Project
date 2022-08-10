package com.unobank.orchestrator_service.dto;

import com.unobank.orchestrator_service.payload.request.TransactionRequest;
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

    public TransactionMessage(String eventName, String messageType, int responseType,
                              String message, String transactionId, TransactionRequest request) {
        this.eventName = eventName;
        this.messageType = messageType;
        this.responseType = responseType;
        this.message = message;

        this.data = new TransactionInfo(transactionId, request.getSenderCardId(), request.getReceiverCardId(), request.getAmount());
    }
}
