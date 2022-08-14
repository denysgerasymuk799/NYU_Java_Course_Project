package com.unobank.orchestrator_service.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NotificationRequest {
	@NotBlank
	@JsonProperty("last_transaction_id")
	private String lastTransactionId;
}
