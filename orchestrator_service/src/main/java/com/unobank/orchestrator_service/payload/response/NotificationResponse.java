package com.unobank.orchestrator_service.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class NotificationResponse {
    @JsonProperty("last_transaction_id")
    private String lastTransactionId;

    @JsonProperty("new_transactions")
    private ArrayList<JSONObject> newTransactions;
}
