package com.unobank.orchestrator_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;

@Data
@AllArgsConstructor
public class TransactionInfo {
    String transactionId;
    String senderCardId;
    String receiverCardId;
    int amount;
    String createTimestamp;
    Date date;
}
