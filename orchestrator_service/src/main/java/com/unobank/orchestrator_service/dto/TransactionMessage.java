package com.unobank.orchestrator_service.dto;

import com.unobank.orchestrator_service.payload.request.TransactionRequest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Data;

@Data
public class TransactionMessage {
    String eventName;
    String messageType;
    int responseType;
    String producer;
    String message;
    TransactionInfo data;

    public TransactionMessage(String eventName, String messageType, int responseType,
                              String message, String transactionId, TransactionRequest request) {
        this.eventName = eventName;
        this.messageType = messageType;
        this.responseType = responseType;
        this.message = message;

        this.data = new TransactionInfo(transactionId, request.getSenderCardId(), request.getReceiverCardId(),
                request.getAmount(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }
}
