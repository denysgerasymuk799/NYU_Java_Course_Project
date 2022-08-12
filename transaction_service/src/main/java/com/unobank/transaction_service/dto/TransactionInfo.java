package com.unobank.transaction_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.sql.Timestamp;
import java.util.Date;

@Data
//@AllArgsConstructor
public class TransactionInfo {
    String transactionId;
    String senderCardId;
    String receiverCardId;
    int amount;
    Timestamp createTimestamp;
    Date date;

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
