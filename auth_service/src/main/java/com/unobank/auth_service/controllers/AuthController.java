package com.unobank.auth_service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import com.unobank.auth_service.data.entities.User;


@RestController
@RequestMapping("/users")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @PutMapping("/auth_user")
    public ResponseEntity<Object> create(@Valid @RequestBody User user) {
        System.out.println(user.getEmail());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(1).toUri();

        LOGGER.info("Saved user #" + 1);
        return ResponseEntity.created(location).build();
    }
}
