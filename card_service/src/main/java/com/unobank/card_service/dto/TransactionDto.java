package com.unobank.card_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.unobank.card_service.domain_logic.enums.TransactionStatus;
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
    Date date;
    TransactionStatus status;

    public static TransactionDto fromTransactionMessage(TransactionMessage transactionMessage, TransactionStatus status) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setTransactionId(transactionMessage.getData().getTransactionId());
        transactionDto.setSenderCardId(transactionMessage.getData().getSenderCardId());
        transactionDto.setReceiverCardId(transactionMessage.getData().getReceiverCardId());
        transactionDto.setAmount(transactionMessage.getData().getAmount());
        transactionDto.setDate(transactionMessage.getData().getDate());
        transactionDto.setStatus(status);

        Date date = new Date();
        transactionDto.setCreateTimestamp(new Timestamp(date.getTime()));

        return transactionDto;
    }
}
