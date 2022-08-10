package com.unobank.orchestrator_service.payload.response;

import lombok.Data;

@Data
public class SuccessfulTransactionResponse {
    private String transactionId;

    public SuccessfulTransactionResponse(String transactionId) {
        this.transactionId = transactionId;
    }
}
