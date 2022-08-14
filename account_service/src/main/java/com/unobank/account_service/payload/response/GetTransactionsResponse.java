package com.unobank.account_service.payload.response;

import com.unobank.account_service.database.models.TransactionRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class GetTransactionsResponse {
    ArrayList<TransactionRecord> transactions;
}
