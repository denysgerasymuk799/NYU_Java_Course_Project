package com.unobank.transaction_service.domain_logic;

import org.springframework.beans.factory.annotation.Autowired;

import com.unobank.transaction_service.database.TransactionServiceOperator;
import com.unobank.transaction_service.domain_logic.enums.Events;
import com.unobank.transaction_service.domain_logic.enums.TransactionStatus;
import com.unobank.transaction_service.dto.ProcessingTransactionMessage;
import com.unobank.transaction_service.dto.TransactionDto;
import com.unobank.transaction_service.dto.TransactionMessage;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    @Autowired
    private TransactionServiceOperator operator;

    public ProcessingTransactionMessage createTransaction(TransactionMessage transaction) {
        TransactionDto transactionDto = TransactionDto.fromTransactionMessage(transaction, TransactionStatus.NEW);

        // Create an entry in the Transaction table with NEW status
        operator.createTransactionRecord(transactionDto);
        return new ProcessingTransactionMessage(
                Events.TRANSACTION_CREATED.label, Constants.MESSAGE_TYPE_REQUEST, Constants.RESPONSE_SUCCESS,
                Constants.TRANSACTION_SERVICE_PRODUCER_NAME, "", transactionDto);
    }
}
