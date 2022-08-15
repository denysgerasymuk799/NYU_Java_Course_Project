package com.unobank.orchestrator_service.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class NotificationRequest {
	@JsonProperty("last_transaction_id")
	private String lastTransactionId;
}
