package com.unobank.orchestrator_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.orchestrator_service.domain_logic.CustomValidator;
import com.unobank.orchestrator_service.payload.request.TransactionRequest;
import com.unobank.orchestrator_service.payload.response.MessageResponse;
import com.unobank.orchestrator_service.security.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;


/**
 *
 * @version 1.0
 */

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/orchestrator")
public class OrchestratorController {
	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private CustomValidator validator;

	@PostMapping("/handle_transaction")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
//	public String handleTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {
	public ResponseEntity<?> handleTransaction(HttpServletRequest request) {
		log.info("Process a new transaction.");
		String jwt = parseJwt(request);
		String username = jwtUtils.getUserNameFromJwtToken(jwt);
		log.info("User {} is making a transaction", username);

		Claims jwtClaims = jwtUtils.getAllClaimsFromToken(jwt);
		LinkedHashMap<String, String> userDetails = jwtClaims.get("user_details", LinkedHashMap.class);

		ObjectMapper mapper = new ObjectMapper();
		TransactionRequest transactionRequest;
		try {
//			String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			transactionRequest = mapper.readValue(request.getReader(), TransactionRequest.class);
			ArrayList<String> violationMessages = validator.validateRequest(transactionRequest);
			if (violationMessages.size() != 0) {
				String errMessages = violationMessages.stream().collect(Collectors.joining("  \n"));
				return new ResponseEntity<>(
						"Input fields are in an incorrect format. Error messages: " + errMessages,
						HttpStatus.BAD_REQUEST);
			}

			if (userDetails.get("card_id").equals(transactionRequest.getSenderCardId())) {
				return new ResponseEntity<>(
						"Authorized user card id is not equal to sender card id in the request body.",
						HttpStatus.METHOD_NOT_ALLOWED);
			}

			System.out.println("transactionRequest: " + transactionRequest);
			System.out.println("transactionRequest amount: " + transactionRequest.getAmount());
		} catch (IOException e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(
					"Can not assign a card. Please try again in 5 minutes.",
					HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}
		return null;
	}
}
