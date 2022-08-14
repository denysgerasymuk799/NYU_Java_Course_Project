package com.unobank.orchestrator_service.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.simple.JSONObject;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class NotificationResponse {
    private String lastTransactionId;
    private ArrayList<JSONObject> newNotifications;
}
