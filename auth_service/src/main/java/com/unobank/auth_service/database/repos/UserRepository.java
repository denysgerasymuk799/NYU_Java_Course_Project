package com.unobank.auth_service.database.repos;

import com.unobank.auth_service.database.models.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

// This interface returns Flux and Mono instead of Lists and objects
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmail(String email);
    void deleteByEmail(String email);
}
