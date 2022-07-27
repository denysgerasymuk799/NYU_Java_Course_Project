package com.unobank.auth_service.database.repos;

import com.unobank.auth_service.database.models.Card;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.stream.Stream;

public interface RegistrationCardsRepository extends MongoRepository<Card, String> {
    Stream<Card> findOneByEnabled(boolean enabled);
}
