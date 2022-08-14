package com.unobank.transaction_service.database.models;

import com.unobank.transaction_service.domain_logic.Utils;
import com.unobank.transaction_service.domain_logic.enums.TransactionStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionRecord {
    private String transactionId;
    private String senderCardId;
    private String receiverCardId;
    private int amount;
    private TransactionStatus status;
    private String date;

    public TransactionRecord(String transactionId, String senderCardId, String receiverCardId, int amount,
                             TransactionStatus status, LocalDate originalDate) {
        this.transactionId = transactionId;
        this.senderCardId = senderCardId;
        this.receiverCardId = receiverCardId;
        this.amount = amount;
        this.status = status;
        this.date = originalDate.toString();
    }
}
