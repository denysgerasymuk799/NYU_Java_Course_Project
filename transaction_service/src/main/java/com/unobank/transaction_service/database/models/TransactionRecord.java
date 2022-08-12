package com.unobank.transaction_service.database.models;

import com.unobank.transaction_service.domain_logic.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Data
@AllArgsConstructor
public class TransactionRecord {
    private String transactionId;
    private String senderCardId;
    private String receiverCardId;
    private int amount;
    private TransactionStatus status;
    private LocalDate originalDate;
    private Date date;

    public TransactionRecord(String transactionId, String senderCardId, String receiverCardId, int amount,
                             TransactionStatus status, LocalDate originalDate) {
        this.date = Date.from(originalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
