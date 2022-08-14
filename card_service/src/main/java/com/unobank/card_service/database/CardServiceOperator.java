package com.unobank.card_service.database;

import com.datastax.oss.driver.api.core.cql.Row;
import com.unobank.card_service.domain_logic.Utils;
import com.unobank.card_service.dto.TransactionDto;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class CardServiceOperator {
    @Autowired
    private CassandraClient cassandraClient;
    private Dotenv dotenv;
    private final String reservedTransactionTable;
    private final String cardTable;

    public CardServiceOperator() {
        this.dotenv = Dotenv
                .configure()
                .directory("./")
                .load();
        this.reservedTransactionTable = dotenv.get("RESERVED_TR_TABLE");
        this.cardTable = dotenv.get("CARD_TABLE");
    }

    public boolean topupBalance(TransactionDto transaction) {
        // Validate user input parameters to prevent SQL injections
        if ((! Utils.isNumeric(transaction.getSenderCardId())) || (transaction.getAmount() <= 0)) {
            return false;
        }
        // Get cardholder current credit limit to carry operation on
        String query = String.format("SELECT credit_limit FROM %s WHERE card_id = '%s'", this.cardTable, transaction.getSenderCardId());
        List<Row> results = cassandraClient.selectQuery(query);
        if (results.size() == 0) {
            log.error("Card {} is not registered", transaction.getSenderCardId());
            return false;
        }
        int cardholderCreditLimit = results.get(0).getInt("credit_limit");

        // Top up senderCardId
        query = String.format("UPDATE %s " +
                        "SET credit_limit = %d " +
                        "WHERE card_id = '%s'",
                this.cardTable, cardholderCreditLimit + transaction.getAmount(), transaction.getSenderCardId());
        cassandraClient.executeInsertQuery(query);
        return true;
    }

    public int getAvailableCardBalance(TransactionDto transaction) {
        // Get an amount of all reserved transactions
        String query = String.format("SELECT amount FROM %s WHERE card_id = ?", this.reservedTransactionTable);
        List<Row> results = cassandraClient.selectWithOneArg(query, transaction.getSenderCardId());

        int reservedAmount = 0;
        for (Row row : results) {
            reservedAmount += row.getInt("amount");
        }

        // Get an available card credit limit
        query = String.format("SELECT credit_limit FROM %s WHERE card_id = ?", this.cardTable);
        results = cassandraClient.selectWithOneArg(query, transaction.getSenderCardId());
        System.out.println("results.size(): " + results.size());
        int creditLimit = results.get(0).getInt("credit_limit");
        // Available limit = Current credit limit - SUM(reserved transactions)
        return creditLimit - reservedAmount;
    }

    public String reserveTransactionAmount(TransactionDto transaction) {
        // Check if there is enough balance
        int currentLimit = this.getAvailableCardBalance(transaction);
        if ((currentLimit - transaction.getAmount()) < 0)
            return null;

        // If enough balance -> Reserve transaction
        String query = String.format("INSERT INTO %s (transaction_id, card_id, receiver_card_id, amount, date) " +
                        "VALUES ('%s', '%s', '%s', %d, '%s')",
                this.reservedTransactionTable, transaction.getTransactionId(), transaction.getSenderCardId(),
                transaction.getReceiverCardId(), transaction.getAmount(), transaction.getDate());
        cassandraClient.executeInsertQuery(query);

        // Select an inserted transaction, check that it exists and return its transactionId
        query = String.format("SELECT transaction_id FROM %s " +
                "WHERE card_id = ? AND transaction_id = '%s'",
                this.reservedTransactionTable, transaction.getTransactionId());
        List<Row> results = cassandraClient.selectWithOneArg(query, transaction.getSenderCardId());
        if (results.size() <= 0) {
            return null;
        }
        return results.get(0).getString("transaction_id");
    }

    public boolean executeTransaction(TransactionDto transactionInfo) {
        // Get the transaction that needs to be executed
        String query = String.format("SELECT transaction_id, card_id, receiver_card_id, amount, date " +
                        "FROM %s " +
                        "WHERE card_id = '%s' AND transaction_id = '%s'",
                this.reservedTransactionTable, transactionInfo.getSenderCardId(), transactionInfo.getTransactionId());
        List<Row> results = cassandraClient.selectQuery(query);
        System.out.println("executeTransaction() results.size(): " + results.size());
        if (results.size() <= 0) {
            return false;
        }
        Row transactionRow = results.get(0);

        // Parse the transaction and create DTO to execute it
        TransactionDto transaction = new TransactionDto();
        transaction.setTransactionId(transactionRow.getString("transaction_id"));
        transaction.setSenderCardId(transactionRow.getString("card_id"));
        transaction.setReceiverCardId(transactionRow.getString("receiver_card_id"));
        transaction.setAmount(transactionRow.getInt("amount"));
        transaction.setDate(Objects.requireNonNull(transactionRow.getLocalDate("date")).toString());

        // Get cardholder current credit limit to carry operation on
        query = String.format("SELECT credit_limit FROM %s WHERE card_id = '%s'",
                this.cardTable, transaction.getSenderCardId());
        results = cassandraClient.selectQuery(query);
        if (results.size() <= 0) {
            return false;
        }
        int cardholderCreditLimit = results.get(0).getInt("credit_limit");

        // Get receiver current credit limit
        query = String.format("SELECT credit_limit FROM %s WHERE card_id = '%s'",
                this.cardTable, transaction.getReceiverCardId());
        results = cassandraClient.selectQuery(query);
        if (results.size() <= 0) {
            return false;
        }
        int receiverCreditLimit = results.get(0).getInt("credit_limit");

        // Withdraw money from the cardholder card and deposit money to the receiver card
        query = String.format("UPDATE %s " +
                        "SET credit_limit = %d " +
                        "WHERE card_id = '%s'",
                this.cardTable, cardholderCreditLimit - transaction.getAmount(), transaction.getSenderCardId());
        cassandraClient.executeInsertQuery(query);

        query = String.format("UPDATE %s " +
                        "SET credit_limit = %d " +
                        "WHERE card_id = '%s'",
                this.cardTable, receiverCreditLimit + transaction.getAmount(), transaction.getReceiverCardId());
        cassandraClient.executeInsertQuery(query);

        // Cancel reservation
        this.cancelReservation(transaction);
        return true;
    }

    public void cancelReservation(TransactionDto transaction) {
        String query = String.format("DELETE FROM %s " +
                        "WHERE card_id = '%s' AND transaction_id = '%s'",
                this.reservedTransactionTable, transaction.getSenderCardId(), transaction.getTransactionId());
        cassandraClient.executeInsertQuery(query);
    }
}
