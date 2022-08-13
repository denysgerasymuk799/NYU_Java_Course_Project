package com.unobank.card_service.dto;

import lombok.Data;
import java.util.Date;

@Data
//@AllArgsConstructor
public class TransactionInfo {
    String transactionId;
    String senderCardId;
    String receiverCardId;
    int amount;
    String createTimestamp;
    Date date;

    public TransactionInfo() {}

    public TransactionInfo(String transactionId, String senderCardId, String receiverCardId,
                           int amount, String createTimestamp) {
        this.transactionId = transactionId;
        this.senderCardId = senderCardId;
        this.receiverCardId = receiverCardId;
        this.amount = amount;
        this.createTimestamp = createTimestamp;
    }
}
