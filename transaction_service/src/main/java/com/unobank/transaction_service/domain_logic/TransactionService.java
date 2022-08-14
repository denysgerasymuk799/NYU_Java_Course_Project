package com.unobank.transaction_service.domain_logic;

import com.unobank.transaction_service.database.models.TransactionRecord;
import com.unobank.transaction_service.domain_logic.enums.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.unobank.transaction_service.database.TransactionServiceOperator;
import com.unobank.transaction_service.domain_logic.enums.Events;
import com.unobank.transaction_service.domain_logic.enums.TransactionStatus;
import com.unobank.transaction_service.dto.ProcessingTransactionMessage;
import com.unobank.transaction_service.dto.TransactionDto;

@Slf4j
@Service
public class TransactionService {
    @Autowired
    private TransactionServiceOperator operator;

    /**
     * Create balance top up transaction record in the database.
     * Notify card service to top up balance for the current transaction.
     * @param transaction: transaction parameters.
     * @return ProcessingTransactionMessage for CardService.
     */
    public ProcessingTransactionMessage createTopupTransaction(ProcessingTransactionMessage transaction) {
        // Create TransactionDto from an input message for processing
        TransactionDto transactionDto = TransactionDto.fromTransactionMessage(transaction, TransactionStatus.NEW);

        // Create an entry in the Transaction table with NEW status
        operator.createTransactionRecord(transactionDto);

        // Create a new TransactionDto with appropriate attributes like status and construct a new message to send to Card service
        TransactionDto newTransactionDto = new TransactionDto();
        newTransactionDto.setTransactionId(transactionDto.getTransactionId());
        newTransactionDto.setSenderCardId(transactionDto.getSenderCardId());
        newTransactionDto.setReceiverCardId(TransactionType.TOP_UP.toString());
        newTransactionDto.setAmount(transactionDto.getAmount());
        newTransactionDto.setStatus(TransactionStatus.NEW.toString());
        newTransactionDto.setCreateTimestamp(transactionDto.getCreateTimestamp());
        newTransactionDto.setDate(transactionDto.getDate());

        log.info("Transaction: [{}]. Status: {}.", transaction.getData().getTransactionId(), TransactionStatus.NEW);
        return new ProcessingTransactionMessage(
                Events.TRANSACTION_TOPUP.label, Constants.MESSAGE_TYPE_REQUEST, Constants.RESPONSE_SUCCESS,
                Constants.TRANSACTION_SERVICE_PRODUCER_NAME, "Transaction status is NEW", newTransactionDto);
    }

    /**
     * Create transaction record in the database. Notify card service to reserve balance for the current transaction.
     * @param transaction: transaction parameters.
     * @return ProcessingTransactionMessage for CardService.
     */
    public ProcessingTransactionMessage createTransaction(ProcessingTransactionMessage transaction) {
        TransactionDto transactionDto = TransactionDto.fromTransactionMessage(transaction, TransactionStatus.NEW);

        // Create an entry in the Transaction table with NEW status
        operator.createTransactionRecord(transactionDto);
        log.info("Transaction: [{}]. Status: {}.", transaction.getData().getTransactionId(), TransactionStatus.NEW);
        return new ProcessingTransactionMessage(
                Events.TRANSACTION_CREATED.label, Constants.MESSAGE_TYPE_REQUEST, Constants.RESPONSE_SUCCESS,
                Constants.TRANSACTION_SERVICE_PRODUCER_NAME, "Transaction status is NEW", transactionDto);
    }

    /**
     * Create a message to execute transaction to CardService. Update transaction status appropriately.
     * @param transaction: transaction parameters.
     * @return ProcessingTransactionMessage for CardService.
     */
    public ProcessingTransactionMessage executeTransaction(ProcessingTransactionMessage transaction) {
        // Get a transaction record
        TransactionDto transactionDto = TransactionDto.fromTransactionMessage(transaction, TransactionStatus.PENDING);
        TransactionRecord record = operator.getTransactionRecord(transactionDto);

        // Mark a transaction as such that is waiting to be executed
        operator.updateTransactionStatus(transactionDto, TransactionStatus.PENDING);

        // Create a new TransactionDto with required appropriate attributes and construct a new message to send to Card service
        TransactionDto newTransactionDto = new TransactionDto();
        newTransactionDto.setCreateTimestamp(transactionDto.getCreateTimestamp());
        newTransactionDto.setTransactionId(transactionDto.getTransactionId());
        newTransactionDto.setSenderCardId(transactionDto.getSenderCardId());
        newTransactionDto.setReceiverCardId(transactionDto.getReceiverCardId());
        newTransactionDto.setAmount(record.getAmount());
        newTransactionDto.setDate(record.getDate());

        log.info("Transaction: [{}]. Status: {}.", record.getTransactionId(), TransactionStatus.PENDING);
        return new ProcessingTransactionMessage(
                Events.TRANSACTION_PENDING.label, Constants.MESSAGE_TYPE_REQUEST, Constants.RESPONSE_SUCCESS,
                Constants.TRANSACTION_SERVICE_PRODUCER_NAME, "Transaction is pending", newTransactionDto);
    }

    /**
     * Create a message with transaction execution status to the Results topic.
     * @param transaction: transaction parameters.
     * @return ProcessingTransactionMessage for CardService.
     */
    public ProcessingTransactionMessage sendTransactionResult(ProcessingTransactionMessage transaction) {
        // Get transaction record
        TransactionDto transactionDto = TransactionDto.fromTransactionMessage(transaction, TransactionStatus.COMPLETED);
        TransactionRecord record = operator.getTransactionRecord(transactionDto);
        System.out.println("record: " + record);

        // Save transaction record in successful transaction table in case transaction is successful
        if (record.getStatus() == TransactionStatus.COMPLETED) {
            operator.saveSuccessfulTransaction(transactionDto);
        }

        // Create a new TransactionDto with required appropriate attributes and construct a new message to send to Card service
        TransactionDto newTransactionDto = new TransactionDto();
        newTransactionDto.setCreateTimestamp(transactionDto.getCreateTimestamp());
        newTransactionDto.setTransactionId(transactionDto.getTransactionId());
        newTransactionDto.setSenderCardId(transactionDto.getSenderCardId());
        newTransactionDto.setReceiverCardId(
                (transactionDto.getSenderCardId().equals(transactionDto.getReceiverCardId()))
                        ? TransactionType.TOP_UP.toString() : transactionDto.getReceiverCardId());
        newTransactionDto.setAmount(record.getAmount());
        newTransactionDto.setDate(record.getDate());

        String message;
        if (record.getStatus() == TransactionStatus.COMPLETED) {
            newTransactionDto.setStatus(TransactionStatus.COMPLETED.toString());
            message = "Transaction is successful";
        } else {
            newTransactionDto.setStatus(TransactionStatus.FAILED.toString());
            message = transaction.getMessage();
        }
        return new ProcessingTransactionMessage(
                Events.TRANSACTION_COMPLETED.label, Constants.MESSAGE_TYPE_REQUEST, Constants.RESPONSE_SUCCESS,
                Constants.TRANSACTION_SERVICE_PRODUCER_NAME, message, newTransactionDto);
    }

    /**
     * Implicitly set the transaction status and send the results to the Results topic.
     * @param transaction: transaction parameters.
     * @param status: transaction current status.
     * @return ProcessingTransactionMessage for CardService.
     */
    public ProcessingTransactionMessage setTransactionCompletionStatus(ProcessingTransactionMessage transaction, TransactionStatus status) {
        TransactionDto transactionDto = TransactionDto.fromTransactionMessage(transaction, status);
        operator.updateTransactionStatus(transactionDto, status);

        ProcessingTransactionMessage message = this.sendTransactionResult(transaction);
        log.info("Transaction: [{}]. Status: {}.", transaction.getData().getTransactionId(), status);
        return message;
    }
}
