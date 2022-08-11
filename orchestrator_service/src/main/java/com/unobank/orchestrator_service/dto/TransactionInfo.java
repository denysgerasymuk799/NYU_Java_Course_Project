package com.unobank.orchestrator_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class TransactionInfo {
    String transactionId;
    String senderCardId;
    String receiverCardId;
    float amount;
    Timestamp createTimestamp;
}
