package com.unobank.account_service.controllers;

import com.unobank.account_service.database.models.TransactionRecord;
import com.unobank.account_service.domain_logic.AccountService;
import com.unobank.account_service.payload.request.GetBalanceRequest;
import com.unobank.account_service.payload.request.GetTransactionsRequest;
import com.unobank.account_service.payload.response.GetBalanceResponse;
import com.unobank.account_service.payload.response.GetTransactionsResponse;
import com.unobank.account_service.security.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/account_service")
@Tag(name = "Account Service REST Endpoint")
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
	@Operation(summary = "Computes user balance", description = "Takes a user card id and returns his card balance.")
	public ResponseEntity<?> getBalance(HttpServletRequest request) {
		log.info("Get balance request.");
		String jwt = parseJwt(request);
		String username = jwtUtils.getUserNameFromJwtToken(jwt);
		log.info("Request for user {}", username);

		Claims jwtClaims = jwtUtils.getAllClaimsFromToken(jwt);
		LinkedHashMap<String, String> userDetails = jwtClaims.get("user_details", LinkedHashMap.class);

		Integer userBalance = 0;
		try {
			GetBalanceRequest getBalanceRequest = new GetBalanceRequest(request.getParameter("card_id"));
			userBalance = accountService.getBalance(getBalanceRequest.getCardId(), userDetails.get("cardId"));
			if (userBalance == null) {
				return new ResponseEntity<>("Invalid input cardId: it is not numeric", HttpStatus.BAD_REQUEST);
			}
			if (userBalance == -1) {
				return new ResponseEntity<>("Input cardId is not equal to signed in user card id", HttpStatus.UNAUTHORIZED);
			}
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
	@Operation(summary = "Collects user top transactions",
			description = "Takes a user card id and start index for the first top transaction, and returns his card balance.")
	public ResponseEntity<?> getTransactions(HttpServletRequest request) {
		log.info("Get transactions request.");
		String jwt = parseJwt(request);
		String username = jwtUtils.getUserNameFromJwtToken(jwt);
		log.info("Request for user {}", username);

		Claims jwtClaims = jwtUtils.getAllClaimsFromToken(jwt);
		LinkedHashMap<String, String> userDetails = jwtClaims.get("user_details", LinkedHashMap.class);

		ArrayList<TransactionRecord> topTransactions = new ArrayList<>();
		try {
			GetTransactionsRequest getTransactionsRequest = new GetTransactionsRequest(
					request.getParameter("card_id"), Integer.parseInt(request.getParameter("start_idx")));
			topTransactions = accountService.getTopTransactions(getTransactionsRequest.getCardId(), userDetails.get("cardId"),
					getTransactionsRequest.getStartIdx());
			if (topTransactions == null) {
				return new ResponseEntity<>("Invalid input cardId", HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			log.error(e.toString());
			return new ResponseEntity<>(
					"Incorrect transaction. Error message: " + e.toString(),
					HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.ok(new GetTransactionsResponse(topTransactions));
	}
}
