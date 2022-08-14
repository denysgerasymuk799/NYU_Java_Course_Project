package com.unobank.account_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDto {
    String transactionId;
    String senderCardId;
    String receiverCardId;
    int amount;
    String createTimestamp;
    String date;
    String status;
}
