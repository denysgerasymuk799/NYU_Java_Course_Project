package com.unobank.orchestrator_service.payload.response;

import lombok.Data;

@Data
public class MessageResponse {
	private String message;

	public MessageResponse(String message) {
	    this.message = message;
	  }
}
