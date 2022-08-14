package com.unobank.orchestrator_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.orchestrator_service.domain_logic.TransactionsStorageManager;
import com.unobank.orchestrator_service.payload.response.NotificationResponse;
import com.unobank.orchestrator_service.payload.request.NotificationRequest;
import com.unobank.orchestrator_service.payload.response.SuccessfulTransactionResponse;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import com.unobank.orchestrator_service.domain_logic.TransactionHandler;
import com.unobank.orchestrator_service.payload.request.TransactionRequest;
import com.unobank.orchestrator_service.security.jwt.JwtUtils;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/orchestrator")
public class OrchestratorController {
	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private TransactionHandler transactionHandler;

	@Autowired
	private TransactionsStorageManager storageManager;

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}
		return null;
	}

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

	@GetMapping("/get_notifications")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<?> getNotifications(HttpServletRequest request) {
		log.info("Get notifications");
		String jwt = parseJwt(request);
		String username = jwtUtils.getUserNameFromJwtToken(jwt);
		log.info("Find notifications for user {}", username);

		Claims jwtClaims = jwtUtils.getAllClaimsFromToken(jwt);
		LinkedHashMap<String, String> userDetails = jwtClaims.get("user_details", LinkedHashMap.class);

		NotificationResponse result = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			NotificationRequest notificationRequest = mapper.readValue(request.getReader(), NotificationRequest.class);
			String userCardId = userDetails.get("cardId");
			if ((userCardId == null) || (userCardId.length() <= 1)) {
				return new ResponseEntity<>("User is unauthorized", HttpStatus.UNAUTHORIZED);
			}
			result = storageManager.getNotifications(userCardId, notificationRequest.getLastTransactionId());
			if (result == null) {
				return new ResponseEntity<>("Transaction with input transaction id was not processed for userCardId",
						HttpStatus.BAD_REQUEST);
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
		return ResponseEntity.ok(result);
	}
}
