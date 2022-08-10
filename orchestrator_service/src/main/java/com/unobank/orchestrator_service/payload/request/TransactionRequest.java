package com.unobank.orchestrator_service.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unobank.orchestrator_service.domain_logic.enums.TransactionType;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TransactionRequest {
	@NotBlank
	@JsonProperty("sender_card_id")
	@Size(min = 16, max = 16)
	private String senderCardId;

	@NotBlank
	@JsonProperty("receiver_card_id")
	@Size(min = 16, max = 16)
	private String receiverCardId;

	@JsonProperty("amount")
	@DecimalMin("1.0")
	private float amount;

	@JsonProperty("transaction_type")
	private TransactionType transactionType;
}
