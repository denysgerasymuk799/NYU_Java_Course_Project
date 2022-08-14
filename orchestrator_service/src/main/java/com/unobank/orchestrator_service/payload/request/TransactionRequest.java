package com.unobank.orchestrator_service.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unobank.orchestrator_service.domain_logic.enums.TransactionType;
import lombok.Data;

import javax.validation.constraints.*;

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
	@Min(1)
	@Positive
	private int amount;

	@JsonProperty("transaction_type")
	private TransactionType transactionType;
}
