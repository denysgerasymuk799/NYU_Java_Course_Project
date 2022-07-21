package com.unobank.auth_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unobank.auth_service.database.dto.UserDto;
import com.unobank.auth_service.database.models.User;
import com.unobank.auth_service.services.UserService;
import reactor.core.publisher.Mono;

/**
 * REST controller for requests of simple users.
 *
 * @version 1.0
 */

@RestController
@RequestMapping(value = "/api/v1/users/")
public class UserRestControllerV1 {
    private final UserService userService;

    @Autowired
    public UserRestControllerV1(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "{id}")
    public Mono<ResponseEntity<UserDto>> getUserById(@PathVariable(name = "id") String id){
        return userService.findById(id)
                .flatMap(user -> {
                    if (user.getEmail().length() > 0) {
                        UserDto result = UserDto.fromUser(user);
                        return Mono.just(new ResponseEntity<>(result, HttpStatus.OK));
                    } else {
                        return Mono.just(new ResponseEntity<UserDto>(HttpStatus.NO_CONTENT));
                    }
                });
    }
}
