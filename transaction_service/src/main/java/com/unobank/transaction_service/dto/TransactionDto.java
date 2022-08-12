package com.unobank.transaction_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.unobank.transaction_service.domain_logic.enums.TransactionStatus;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDto {
    String transactionId;
    String senderCardId;
    String receiverCardId;
    int amount;
    Timestamp createTimestamp;
    TransactionStatus status;

    public static TransactionDto fromTransactionMessage(TransactionMessage transactionMessage, TransactionStatus status) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setTransactionId(transactionMessage.getData().getTransactionId());
        transactionDto.setSenderCardId(transactionMessage.getData().getSenderCardId());
        transactionDto.setReceiverCardId(transactionMessage.getData().getReceiverCardId());
        transactionDto.setAmount(transactionMessage.getData().getAmount());
        transactionDto.setStatus(status);

        Date date = new Date();
        transactionDto.setCreateTimestamp(new Timestamp(date.getTime()));

        return transactionDto;
    }
}