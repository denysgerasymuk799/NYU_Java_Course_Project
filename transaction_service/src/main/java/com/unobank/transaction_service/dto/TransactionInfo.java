package com.unobank.transaction_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.sql.Timestamp;

@Data
//@AllArgsConstructor
public class TransactionInfo {
    String transactionId;
    String senderCardId;
    String receiverCardId;
    int amount;
    Timestamp createTimestamp;

    public TransactionInfo() {}

    public TransactionInfo(String transactionId, String senderCardId, String receiverCardId,
                           int amount, Timestamp createTimestamp) {
        this.transactionId = transactionId;
        this.senderCardId = senderCardId;
        this.receiverCardId = receiverCardId;
        this.amount = amount;
        this.createTimestamp = createTimestamp;
    }
}
