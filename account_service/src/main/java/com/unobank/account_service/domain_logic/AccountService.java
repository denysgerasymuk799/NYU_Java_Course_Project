package com.unobank.account_service.domain_logic;

import com.unobank.account_service.database.AccountServiceOperator;
import com.unobank.account_service.database.models.TransactionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
public class AccountService {
    @Autowired
    private AccountServiceOperator operator;

    public Integer getBalance(String inputCardId, String userCardId) {
        if (!Utils.isNumeric(inputCardId))
            return null;

        if (!inputCardId.equals(userCardId))
            return -1;

        return operator.getBalance(inputCardId);
    }

    public ArrayList<TransactionRecord> getTopTransactions(String inputCardId, String userCardId, int topTransactionsStartIdx) {
        if (!Utils.isNumeric(inputCardId)) {
            log.error("Invalid input cardId: it is not numeric");
            return null;
        }

        if (!inputCardId.equals(userCardId)) {
            log.error("Input cardId is not equal to signed in user card id");
            return null;
        }

        return operator.getTransactionForCard(inputCardId, topTransactionsStartIdx);
    }
}
