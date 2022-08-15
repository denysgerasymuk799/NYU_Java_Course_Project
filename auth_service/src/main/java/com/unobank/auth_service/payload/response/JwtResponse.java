package com.unobank.auth_service.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
	private String token;
	private String type = "Bearer";
	private String id;
	private String username;
	@JsonProperty("card_id")
	private String cardId;
	private String email;
	private List<String> roles;

	public JwtResponse(String accessToken, String id, String username, String cardId, String email, List<String> roles) {
		this.token = accessToken;
		this.id = id;
		this.username = username;
		this.cardId = cardId;
		this.email = email;
		this.roles = roles;
	}
}
