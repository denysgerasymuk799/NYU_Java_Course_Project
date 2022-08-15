package com.unobank.transaction_service.database;

import com.datastax.oss.driver.api.core.cql.Row;
import com.unobank.transaction_service.database.models.TransactionRecord;
import com.unobank.transaction_service.domain_logic.Utils;
import com.unobank.transaction_service.domain_logic.enums.TransactionStatus;
import com.unobank.transaction_service.domain_logic.enums.TransactionType;
import com.unobank.transaction_service.dto.TransactionDto;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Operator, which provides all interactions with AWS Keyspaces
 */

@Component
public class TransactionServiceOperator {
    @Autowired
    private CassandraClient cassandraClient;
    private Dotenv dotenv;
    private final String cardTable;
    private final String transactionTable;
    private final String transactionByCardTable;
    private final String successfulTransactionsDailyTable;

    public TransactionServiceOperator() {
        this.dotenv = Dotenv
                .configure()
                .directory("./")
                .load();
        this.cardTable = dotenv.get("CARD_TABLE");
        this.transactionTable = dotenv.get("TRANSACTIONS_TABLE");
        this.transactionByCardTable = dotenv.get("TRANSACTIONS_BY_CARD_TABLE");
        this.successfulTransactionsDailyTable = dotenv.get("SUCCESSFUL_TRANSACTIONS_DAILY_TABLE");
    }

    public TransactionRecord getTransactionRecord(TransactionDto transaction) {
        // Create a record object from an entry in the database
        String query = String.format("SELECT transaction_id, card_id, receiver_card_id, amount, status, date " +
                "FROM %s " +
                "WHERE transaction_id = ?", transactionTable);
        List<Row> results = cassandraClient.selectWithOneArg(query, transaction.getTransactionId());
        if (results.size() <= 0) {
            return null;
        }
        Row result = results.get(0);
        System.out.println("result: " + result.getFormattedContents());
        return new TransactionRecord(result.getString("transaction_id"), result.getString("card_id"),
                result.getString("receiver_card_id"), result.getInt("amount"),
                TransactionStatus.valueOf(result.getString("status")), Objects.requireNonNull(result.getLocalDate("date")));
    }

    public void createTransactionRecord(TransactionDto transaction) {
        // Validate user input parameters to prevent SQL injections. Since float type is primitive, check default value.
        if ((! Utils.isNumeric(transaction.getReceiverCardId())) || (transaction.getAmount() == 0.0f)) {
            return;
        }
        String query;

        // If activity is not a balance top up
        if (transaction.getReceiverCardId().equals(TransactionType.TOP_UP.toString())) {
            // Check whether such receiver exists
            query = String.format("SELECT * FROM %s WHERE card_id = ?", cardTable);
            List<Row> results = cassandraClient.selectWithOneArg(query, transaction.getReceiverCardId());
            if (results.size() <= 0) {
                return;
            }
        }

        // Insert transaction record into table
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        query = String.format("INSERT INTO %s (transaction_id, card_id, receiver_card_id, amount, status, date) " +
                "VALUES ('%s', '%s', '%s', %d, '%s', '%s')",
                transactionTable, transaction.getTransactionId(), transaction.getSenderCardId(), transaction.getReceiverCardId(),
                transaction.getAmount(), transaction.getStatus(), date);
        cassandraClient.executeInsertQuery(query);

        // In this query we have card_id and sender_card_id, since primary key in Cassandra table is transaction_id and card_id
        query = String.format("INSERT INTO %s (transaction_id, card_id, sender_card_id, receiver_card_id, amount, status, date) " +
                        "VALUES ('%s', '%s', '%s', '%s', %d, '%s', '%s')",
                transactionByCardTable, transaction.getTransactionId(), transaction.getSenderCardId(), transaction.getSenderCardId(),
                transaction.getReceiverCardId(), transaction.getAmount(), transaction.getStatus(), date);
        cassandraClient.executeInsertQuery(query);

        if (transaction.getReceiverCardId().equals(TransactionType.TOP_UP.toString())) {
            query = String.format("INSERT INTO %s (transaction_id, card_id, sender_card_id, receiver_card_id, amount, status, date) " +
                            "VALUES ('%s', '%s', '%s', '%s', %d, '%s', '%s')",
                    transactionByCardTable, transaction.getTransactionId(), transaction.getReceiverCardId(), transaction.getSenderCardId(),
                    transaction.getReceiverCardId(), transaction.getAmount(), transaction.getStatus(), date);
            cassandraClient.executeInsertQuery(query);
        }
    }

    public void updateTransactionStatus(TransactionDto transaction, TransactionStatus status) {
        // Get transaction record
        String query = String.format("SELECT card_id, date, receiver_card_id FROM %s " +
                "WHERE transaction_id = ?", transactionTable);
        List<Row> results = cassandraClient.selectWithOneArg(query, transaction.getTransactionId());
        if (results.size() <= 0) {
            return;
        }

        String senderCardId = results.get(0).getString("card_id");
        String date = Objects.requireNonNull(results.get(0).getLocalDate("date")).toString();
        String receiverCardId = results.get(0).getString("receiver_card_id");
        System.out.println("date: " + date);

        // Update transaction record
        query = String.format("UPDATE %s " +
                "SET status = '%s' " +
                "WHERE transaction_id = '%s' AND card_id = '%s' AND date = '%s'",
                transactionTable, status.toString(), transaction.getTransactionId(), senderCardId, date);
        cassandraClient.executeInsertQuery(query);
        query = String.format("UPDATE %s " +
                        "SET status = '%s' " +
                        "WHERE card_id = '%s' AND date = '%s' AND transaction_id = '%s'",
                transactionByCardTable, status.toString(), senderCardId, date, transaction.getTransactionId());
        cassandraClient.executeInsertQuery(query);

        assert receiverCardId != null;
        if (receiverCardId.equals(TransactionType.TOP_UP.toString())) {
            query = String.format("UPDATE %s " +
                            "SET status = '%s' " +
                            "WHERE card_id = '%s' AND date='%s' AND transaction_id = '%s'",
                    transactionByCardTable, status.toString(), receiverCardId, date, transaction.getTransactionId());
            cassandraClient.executeInsertQuery(query);
        }
    }

    public void saveSuccessfulTransaction(TransactionDto transaction) {
        // Get transaction record
        TransactionRecord record = this.getTransactionRecord(transaction);
        if (record == null)
            return;

        // Save successful transaction with its completion date
        Date date = new Date();
        String query = String.format("INSERT INTO %s (transaction_id, card_id, receiver_card_id, amount, date) " +
                        "VALUES ('%s', '%s', '%s', %d, '%s')",
                successfulTransactionsDailyTable, record.getTransactionId(), record.getSenderCardId(),
                record.getReceiverCardId(), record.getAmount(), Utils.getFormattedDate(date));
        cassandraClient.executeInsertQuery(query);
    }
}
