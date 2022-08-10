package com.unobank.orchestrator_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.orchestrator_service.payload.response.SuccessfulTransactionResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import com.unobank.orchestrator_service.domain_logic.TransactionHandler;
import com.unobank.orchestrator_service.payload.request.TransactionRequest;
import com.unobank.orchestrator_service.payload.response.MessageResponse;
import com.unobank.orchestrator_service.security.jwt.JwtUtils;


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
	private TransactionHandler transactionHandler;

	@PostMapping("/handle_transaction")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<?> handleTransaction(HttpServletRequest request) {
		log.info("Process a new transaction.");
		String jwt = parseJwt(request);
		String transactionId = UUID.randomUUID().toString();

		try {
			ObjectMapper mapper = new ObjectMapper();
			TransactionRequest transactionRequest = mapper.readValue(request.getReader(), TransactionRequest.class);
			String errorMessage = transactionHandler.handleTransaction(jwt, transactionRequest, transactionId);
			if (errorMessage != null) {
				return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
			}
		} catch (IOException e) {
			log.error(e.toString());
			return new ResponseEntity<>(
					"Incorrect type of parameters in the request body. Error message: " + e.toString(),
					HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.error(e.toString());
			return new ResponseEntity<>(
					"Incorrect transaction. Error message: " + e.toString(),
					HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.ok(new SuccessfulTransactionResponse(transactionId));
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}
		return null;
	}
}
