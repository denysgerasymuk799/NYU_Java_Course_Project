package com.unobank.auth_service.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import com.unobank.auth_service.database.models.Role;
import com.unobank.auth_service.database.models.User;
import com.unobank.auth_service.services.UserService;
import com.unobank.auth_service.database.repos.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Implementation of {@link UserService} interface.
 * Wrapper for {@link UserRepository} + business logic.
 *
 * @version 1.0
 */

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> register(User user) {
        user.setHashedPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.simple_user.toString());
//        user.setDisabled(false);
        Mono<User> registeredUser = userRepository.save(user);
        log.info("IN register - user: {} successfully registered", registeredUser);

        return registeredUser;
    }

    @Override
    public Flux<User> getAll() {
        Flux<User> result = userRepository.findAll();
        log.info("IN getAll - users found");
        return result;
    }

    @Override
    public Mono<User> findByEmail(String email) {
        Mono<User> result = userRepository.findByEmail(email);
        log.info("IN findByUsername - user: {} found by username: {}", result, email);
        return result;
    }

    @Override
    public Mono<User> findById(String id) {
        Mono<User> result = userRepository.findById(id);
        log.info("IN findById - user: {} found by id: {}", result, id);
        return result;
    }

    @Override
    public void deleteByEmail(String email) {
         userRepository.deleteByEmail(email);
         log.info("IN delete - user with id: {} successfully deleted", email);
    }
}
