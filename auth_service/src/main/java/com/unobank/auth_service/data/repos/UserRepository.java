package com.unobank.auth_service.data.repos;

import com.unobank.auth_service.data.entities.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

// This interface returns Flux and Mono instead of Lists and objects
public interface UserRepository extends ReactiveMongoRepository<User, String> {
}
