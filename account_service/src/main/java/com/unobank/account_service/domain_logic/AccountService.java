package com.unobank.account_service.domain_logic;

import com.unobank.account_service.database.AccountServiceOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
