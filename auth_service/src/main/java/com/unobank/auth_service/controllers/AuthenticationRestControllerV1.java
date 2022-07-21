package com.unobank.auth_service.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import com.unobank.auth_service.security.jwt.JwtTokenProvider;
import com.unobank.auth_service.database.dto.AuthenticationRequestDto;
import com.unobank.auth_service.database.models.User;
import com.unobank.auth_service.security.jwt.JwtTokenProvider;
import com.unobank.auth_service.services.UserService;
import reactor.core.publisher.Mono;

/**
 * REST controller for authentication requests (login, logout, register, etc.)
 *
 * @author Eugene Suleimanov
 * @version 1.0
 */

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/auth/")
public class AuthenticationRestControllerV1 {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;

    @Autowired
    public AuthenticationRestControllerV1(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("login")
    public Mono<Object> login(@RequestBody AuthenticationRequestDto requestDto) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            return userService.findByEmail(email)
                        .flatMap(user -> {
                                if (user.getEmail().length() > 0) {
                                    String token = jwtTokenProvider.createToken(email, user.getRole().toString());

                                    Map<Object, Object> response = new HashMap<>();
                                    response.put("email", email);
                                    response.put("token", token);

                                    return Mono.just(ResponseEntity.ok(response));
                                } else {
                                    log.error("User is not found. Email: {}", email);
                                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED));
                                }
                        });
        } catch (AuthenticationException e) {
            log.info("Invalid email: {} or password: {}", email, password);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED));
        }
    }
}
