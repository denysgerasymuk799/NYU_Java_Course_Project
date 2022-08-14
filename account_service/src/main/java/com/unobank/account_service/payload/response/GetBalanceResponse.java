package com.unobank.account_service.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetBalanceResponse {
    Integer userBalance;
}
