package com.unobank.card_service.database;

import com.datastax.oss.driver.api.core.cql.Row;
import com.unobank.card_service.database.models.TransactionRecord;
import com.unobank.card_service.domain_logic.Utils;
import com.unobank.card_service.domain_logic.enums.TransactionStatus;
import com.unobank.card_service.domain_logic.enums.TransactionType;
import com.unobank.card_service.dto.TransactionDto;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class CardServiceOperator {
    @Autowired
    private CassandraClient cassandraClient;
    private Dotenv dotenv;
    // TODO: mark as final arguments
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

    public int getAvailableCardBalance(TransactionDto transaction) {
        // Get an amount of all reserved transactions
        String query = String.format("SELECT amount FROM %s WHERE card_id = ?", this.reservedTransactionTable);
        List<Row> results = cassandraClient.selectWithOneArg(query, transaction.getSenderCardId());
        if (results.size() <= 0) {
            return 0;
        }
        int reservedAmount = 0;
        for (Row row : results) {
            reservedAmount += row.getInt("amount");
        }

        // Get an available card credit limit
        query = String.format("SELECT credit_limit FROM %s WHERE card_id = ?", this.cardTable);
        results = cassandraClient.selectWithOneArg(query, transaction.getSenderCardId());
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

        query = String.format("SELECT transaction_id FROM %s " +
                "WHERE card_id = '%s' AND transaction_id = '%s'",
                this.reservedTransactionTable, transaction.getSenderCardId(), transaction.getTransactionId());
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
        List<Row> results = cassandraClient.selectWithOneArg(query, transactionInfo.getSenderCardId());
        if (results.size() <= 0) {
            return false;
        }
        Row transactionRow = results.get(0);
        TransactionDto transaction = new TransactionDto();
        transaction.setTransactionId(transactionRow.getString("transaction_id"));
        transaction.setSenderCardId(transactionRow.getString("card_id"));
        transaction.setReceiverCardId(transactionRow.getString("receiver_card_id"));
        transaction.setAmount(transactionRow.getInt("amount"));
        transaction.setDate(Utils.convertLocalDateToDate(transactionRow.getLocalDate("date")));

        // Get cardholder current credit limit to carry operation on
        query = String.format("SELECT credit_limit FROM %s WHERE card_id = '%s'",
                this.cardTable, transaction.getSenderCardId());
        results = cassandraClient.selectWithOneArg(query, transactionInfo.getSenderCardId());
        if (results.size() <= 0) {
            return false;
        }
        int cardholderCreditLimit = results.get(0).getInt("credit_limit");

        // Get receiver current credit limit
        query = String.format("SELECT credit_limit FROM %s WHERE card_id = '%s'",
                this.cardTable, transaction.getReceiverCardId());
        results = cassandraClient.selectWithOneArg(query, transactionInfo.getSenderCardId());
        if (results.size() <= 0) {
            return false;
        }
        int receiverCreditLimit = results.get(0).getInt("credit_limit");

        // Withdraw money from the cardholder card.
        // Deposit money to the receiver card.
        query = String.format("UPDATE %s " +
                        "SET credit_limit = %d" +
                        "WHERE card_id = '%s'",
                this.cardTable, cardholderCreditLimit - transaction.getAmount(), transaction.getSenderCardId());
        cassandraClient.executeInsertQuery(query);

        query = String.format("UPDATE %s " +
                        "SET credit_limit = %d" +
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
