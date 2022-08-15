package com.unobank.account_service.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetBalanceResponse {
    @JsonProperty("user_balance")
    Integer userBalance;
}
