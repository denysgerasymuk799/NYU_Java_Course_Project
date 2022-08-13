package com.unobank.card_service.dto;

import lombok.Data;

@Data
public class TransactionMessage {
    String eventName;
    String messageType;
    int responseType;
    String producer;
    String message;
    TransactionInfo data;
}
