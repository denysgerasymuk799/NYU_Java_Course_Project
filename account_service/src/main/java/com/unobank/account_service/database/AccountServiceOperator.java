package com.unobank.account_service.database;

import com.datastax.oss.driver.api.core.cql.Row;
import com.unobank.account_service.database.models.TransactionRecord;
import com.unobank.account_service.domain_logic.enums.TransactionStatus;
import com.unobank.account_service.dto.TransactionDto;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class AccountServiceOperator {
    @Autowired
    private CassandraClient cassandraClient;
    private final Dotenv dotenv;
    private final String cardTable;
    private final String reservedTransactionsTable;
    private final String transactionsByCardTable;

    public AccountServiceOperator() {
        this.dotenv = Dotenv
                .configure()
                .directory("./")
                .load();
        this.cardTable = dotenv.get("CARD_TABLE");
        this.reservedTransactionsTable = dotenv.get("RESERVED_TR_TABLE");
        this.transactionsByCardTable = dotenv.get("TRANSACTIONS_BY_CARD_TABLE");
    }

    /**
     * Get user balance as user credit_limit - reserved_sum.
     * @param senderCardId: sender card id.
     * @return user balance.
     */
    public int getBalance(String senderCardId) {
        String query = String.format("SELECT credit_limit FROM %s " +
                        "WHERE card_id = '%s'",
                        this.cardTable, senderCardId);
        List<Row> records = cassandraClient.selectQuery(query);
        int creditLimit = 0;
        if (records.size() != 0) {
            creditLimit = records.get(0).getInt("credit_limit");
        }

        query = String.format("SELECT amount FROM %s " +
                "WHERE card_id = '%s'",
                this.reservedTransactionsTable, senderCardId);
        records = cassandraClient.selectQuery(query);
        int reservedSum = 0;
        if (records.size() != 0) {
            for (Row record : records) {
                reservedSum += record.getInt("amount");
            }
        }
        return creditLimit - reservedSum;
    }

    public ArrayList<TransactionRecord> getTransactionForCard(TransactionDto transaction, int startIdx) {
        String query = String.format("SELECT transaction_id, sender_card_id, receiver_card_id, amount, status, date " +
                        "FROM %s " +
                        "WHERE card_id = '%s'",
                this.transactionsByCardTable, transaction.getSenderCardId());
        List<Row> records = cassandraClient.selectQuery(query);
        int maxIdx = records.size();
        if (startIdx > maxIdx - 1)
            return new ArrayList<>();
        if (records.size() == 0)
            return null;

        ArrayList<TransactionRecord> topTransactions = new ArrayList<>();
        for (Row record : records) {
            TransactionRecord transactionRecord = new TransactionRecord(
                    record.getString("transaction_id"),
                    record.getString("sender_card_id"),
                    record.getString("receiver_card_id"),
                    record.getInt("amount"),
                    TransactionStatus.valueOf(record.getString("status")),
                    Objects.requireNonNull(record.getLocalDate("date"))
            );
            topTransactions.add(transactionRecord);
        }
        return topTransactions;
    }
}
