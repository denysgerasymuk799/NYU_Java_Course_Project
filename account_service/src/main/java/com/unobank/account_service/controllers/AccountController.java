package com.unobank.account_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.account_service.database.models.TransactionRecord;
import com.unobank.account_service.domain_logic.AccountService;
import com.unobank.account_service.payload.request.GetBalanceRequest;
import com.unobank.account_service.payload.request.GetTransactionsRequest;
import com.unobank.account_service.payload.response.GetBalanceResponse;
import com.unobank.account_service.payload.response.GetTransactionsResponse;
import com.unobank.account_service.security.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/account_service")
public class AccountController {
	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private AccountService accountService;

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}
		return null;
	}

	@GetMapping("/get_balance")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<?> getBalance(HttpServletRequest request) {
		log.info("Get balance request.");
		String jwt = parseJwt(request);
		String username = jwtUtils.getUserNameFromJwtToken(jwt);
		log.info("Request for user {}", username);

		Claims jwtClaims = jwtUtils.getAllClaimsFromToken(jwt);
		LinkedHashMap<String, String> userDetails = jwtClaims.get("user_details", LinkedHashMap.class);

		// TODO: check case when cardId is not numeric

		// TODO: add check in all REST microservices for cardId is numeric
		Integer userBalance = 0;
		try {
			ObjectMapper mapper = new ObjectMapper();
			GetBalanceRequest getBalanceRequest = mapper.readValue(request.getReader(), GetBalanceRequest.class);
			userBalance = accountService.getBalance(getBalanceRequest.getCardId(), userDetails.get("cardId"));
			if (userBalance == null) {
				return new ResponseEntity<>("Invalid input cardId: it is not numeric", HttpStatus.BAD_REQUEST);
			}
			if (userBalance == -1) {
				return new ResponseEntity<>("Input cardId is not equal to signed in user card id", HttpStatus.UNAUTHORIZED);
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

		return ResponseEntity.ok(new GetBalanceResponse(userBalance));
	}

	@GetMapping("/get_transactions")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<?> getTransactions(HttpServletRequest request) {
		log.info("Get transactions request.");
		String jwt = parseJwt(request);
		String username = jwtUtils.getUserNameFromJwtToken(jwt);
		log.info("Request for user {}", username);

		Claims jwtClaims = jwtUtils.getAllClaimsFromToken(jwt);
		LinkedHashMap<String, String> userDetails = jwtClaims.get("user_details", LinkedHashMap.class);

		// TODO: check case when cardId is not numeric

		// TODO: add check in all REST microservices for cardId is numeric
		ArrayList<TransactionRecord> topTransactions = new ArrayList<>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			GetTransactionsRequest getTransactionsRequest = mapper.readValue(request.getReader(), GetTransactionsRequest.class);
			topTransactions = accountService.getTopTransactions(getTransactionsRequest.getCardId(), userDetails.get("cardId"),
					getTransactionsRequest.getStartIdx());
			if (topTransactions == null) {
				return new ResponseEntity<>("Invalid input cardId", HttpStatus.BAD_REQUEST);
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

		return ResponseEntity.ok(new GetTransactionsResponse(topTransactions));
	}
}
