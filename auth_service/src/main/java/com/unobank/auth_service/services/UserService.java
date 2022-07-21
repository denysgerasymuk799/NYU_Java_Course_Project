package com.unobank.auth_service.services;

import com.unobank.auth_service.database.models.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service interface for class {@link User}.
 *
 * @author Eugene Suleimanov
 * @version 1.0
 */

public interface UserService {

    Mono<User> register(User user);

    Flux<User> getAll();

    Mono<User> findByEmail(String email);

    Mono<User> findById(String id);

    void deleteByEmail(String email);
}
