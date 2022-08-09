package com.unobank.orchestrator_service.controllers;

import com.unobank.orchestrator_service.security.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


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

	@PostMapping("/handle_transaction")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
//	public String handleTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {
	public String handleTransaction(HttpServletRequest request) {
		log.info("Process a new transaction.");
		String jwt = parseJwt(request);
		String username = jwtUtils.getUserNameFromJwtToken(jwt);
		log.info("User successfully got access with the next username: " + username);

		Claims userDetails = jwtUtils.getAllClaimsFromToken(jwt);
		log.info("User details: " + userDetails);
		return "User OK.";
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}

		return null;
	}
}
