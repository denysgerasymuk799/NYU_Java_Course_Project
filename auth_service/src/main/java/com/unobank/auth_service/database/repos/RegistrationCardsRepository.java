package com.unobank.auth_service.database.repos;

import com.unobank.auth_service.database.models.Card;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RegistrationCardsRepository extends MongoRepository<Card, String> {
    Card findOneByEnabled(boolean enabled);
}
