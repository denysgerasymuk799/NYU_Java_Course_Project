package com.unobank.card_service.domain_logic;

import com.unobank.card_service.database.models.TransactionRecord;
import com.unobank.card_service.domain_logic.enums.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;

import com.unobank.card_service.database.CardServiceOperator;
import com.unobank.card_service.domain_logic.enums.Events;
import com.unobank.card_service.domain_logic.enums.TransactionStatus;
import com.unobank.card_service.dto.ProcessingTransactionMessage;
import com.unobank.card_service.dto.TransactionDto;
import com.unobank.card_service.dto.TransactionMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CardService {
    @Autowired
    private CardServiceOperator operator;

    // TODO: add create_topup_transaction()

    /**
     * Reserve card balance for the newly created transaction.
     * @param transaction: transaction parameters.
     * @return ProcessingTransactionMessage for TransactionService.
     */
    public ProcessingTransactionMessage reserveBalance(ProcessingTransactionMessage transaction) {
        TransactionDto transactionDto = TransactionDto.fromTransactionMessage(transaction, TransactionStatus.RESERVED);

        // Try reserve money
        String transactionId = operator.reserveTransactionAmount(transactionDto);

        // Mark operation event as successful if reserved
        String eventName, message;
        if (transactionId != null) {
            eventName = Events.RESERVATION_SUCCESS.label;
            message = "Operation success.";
        } else {
            eventName = Events.RESERVATION_FAILURE.label;
            message = "CardServiceError: Unable to execute current operation. Not enough balance.";
        }

        // Send a response to a TransactionService
        TransactionDto newTransactionDto = new TransactionDto();
        newTransactionDto.setTransactionId(transactionDto.getTransactionId());
        newTransactionDto.setSenderCardId(transactionDto.getSenderCardId());
        log.info("Transaction: [{}]. Status: {}", transaction.getData().getTransactionId(), TransactionStatus.RESERVED);
        return new ProcessingTransactionMessage(
                eventName, Constants.MESSAGE_TYPE_RESPONSE, Constants.RESPONSE_SUCCESS,
                Constants.CARD_SERVICE_PRODUCER_NAME, message, transactionDto);
    }

    /**
     * Execute transaction.
     * @param transaction: transaction parameters.
     * @return ProcessingTransactionMessage for TransactionService.
     */
    public ProcessingTransactionMessage processPayment(ProcessingTransactionMessage transaction) {
        TransactionDto transactionDto = TransactionDto.fromTransactionMessage(transaction, TransactionStatus.COMPLETED);
        // Try execute transaction
        boolean responseStatus = operator.executeTransaction(transactionDto);
        String eventName, message;
        if (responseStatus) {
            eventName = Events.TRANSACTION_SUCCESS.label;
            message = "Operation success.";
        } else {
            eventName = Events.TRANSACTION_FAILURE.label;
            message = "CardServiceError: Failed to execute transaction.";
        }

        // Send a response to a TransactionService
        TransactionDto newTransactionDto = new TransactionDto();
        newTransactionDto.setTransactionId(transactionDto.getTransactionId());
        newTransactionDto.setSenderCardId(transactionDto.getSenderCardId());
        log.info("Transaction: [{}]. Status: {}", transaction.getData().getTransactionId(), TransactionStatus.COMPLETED);
        return new ProcessingTransactionMessage(
                eventName, Constants.MESSAGE_TYPE_RESPONSE, Constants.RESPONSE_SUCCESS,
                Constants.CARD_SERVICE_PRODUCER_NAME, message, transactionDto);
    }

    /**
     * Cancel transaction reservation by specified cardId and transactionId.
     * @param transaction: transaction parameters.
     * @return ProcessingTransactionMessage for TransactionService.
     */
    public ProcessingTransactionMessage cancelReservation(ProcessingTransactionMessage transaction) {
        TransactionDto transactionDto = TransactionDto.fromTransactionMessage(transaction, TransactionStatus.CANCELLED);
        operator.cancelReservation(transactionDto);

        // Send a response to a TransactionService
        TransactionDto newTransactionDto = new TransactionDto();
        newTransactionDto.setTransactionId(transactionDto.getTransactionId());
        newTransactionDto.setSenderCardId(transactionDto.getSenderCardId());
        log.info("Transaction: [{}]. Status: {}", transaction.getData().getTransactionId(), TransactionStatus.CANCELLED);
        return new ProcessingTransactionMessage(
                Events.TRANSACTION_CANCELLED.label, Constants.MESSAGE_TYPE_RESPONSE, Constants.RESPONSE_SUCCESS,
                Constants.CARD_SERVICE_PRODUCER_NAME, "", transactionDto);
    }
}
