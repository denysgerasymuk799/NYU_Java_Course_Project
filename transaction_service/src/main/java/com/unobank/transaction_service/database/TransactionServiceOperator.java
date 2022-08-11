package com.unobank.transaction_service.database;

import com.datastax.oss.driver.api.core.cql.Row;
import com.unobank.transaction_service.domain_logic.Utils;
import com.unobank.transaction_service.domain_logic.enums.TransactionType;
import com.unobank.transaction_service.dto.TransactionDto;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.List;

public class TransactionServiceOperator {
    @Autowired
    private CassandraClient cassandraClient;
    private Dotenv dotenv;
    private String cardTable;
    private String transactionTable;
    private String transactionByCardTable;

    public TransactionServiceOperator() {
        this.dotenv = Dotenv
                .configure()
                .directory("./")
                .load();
        this.cardTable = dotenv.get("CARD_TABLE");
        this.transactionTable = dotenv.get("TRANSACTIONS_TABLE");
        this.transactionByCardTable = dotenv.get("TRANSACTIONS_BY_CARD_TABLE");
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
            List<Row> results = cassandraClient.selectOne(query, transaction.getReceiverCardId());
            if (results.size() <= 0) {
                return;
            }
        }

        // Insert transaction record into table
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        query = String.format("INSERT INTO %s (transaction_id, card_id, receiver_card_id, amount, status, date) " +
                "VALUES ('%s', '%s', '%s', %s, '%s', '%s')",
                transactionTable, transaction.getTransactionId(), transaction.getSenderCardId(), transaction.getReceiverCardId(),
                transaction.getAmount(), transaction.getStatus(), timestamp);
        cassandraClient.executeInsertQuery(query);

        // In this query we have card_id and sender_card_id, since primary key in Cassandra table is transaction_id and card_id
        query = String.format("INSERT INTO %s (transaction_id, card_id, sender_card_id, receiver_card_id, amount, status, date) " +
                        "VALUES ('%s', '%s', '%s', '%s', %s, '%s', '%s')",
                transactionByCardTable, transaction.getTransactionId(), transaction.getSenderCardId(), transaction.getSenderCardId(),
                transaction.getReceiverCardId(), transaction.getAmount(), transaction.getStatus(), timestamp);
        cassandraClient.executeInsertQuery(query);

        if (transaction.getReceiverCardId().equals(TransactionType.TOP_UP.toString())) {
            query = String.format("INSERT INTO %s (transaction_id, card_id, sender_card_id, receiver_card_id, amount, status, date) " +
                            "VALUES ('%s', '%s', '%s', '%s', %s, '%s', '%s')",
                    transactionByCardTable, transaction.getTransactionId(), transaction.getReceiverCardId(), transaction.getSenderCardId(),
                    transaction.getReceiverCardId(), transaction.getAmount(), transaction.getStatus(), timestamp);
            cassandraClient.executeInsertQuery(query);
        }
    }
}
