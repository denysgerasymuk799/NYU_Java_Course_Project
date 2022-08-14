package com.unobank.transaction_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.unobank.transaction_service.domain_logic.enums.TransactionStatus;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static TransactionDto fromTransactionMessage(ProcessingTransactionMessage transactionMessage, TransactionStatus status) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setTransactionId(transactionMessage.getData().getTransactionId());
        transactionDto.setSenderCardId(transactionMessage.getData().getSenderCardId());
        transactionDto.setReceiverCardId(transactionMessage.getData().getReceiverCardId());
        transactionDto.setAmount(transactionMessage.getData().getAmount());
        transactionDto.setDate(transactionMessage.getData().getDate());
        transactionDto.setStatus(status.toString());

        transactionDto.setCreateTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return transactionDto;
    }
}
