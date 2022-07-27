package com.unobank.auth_service.database.repos;

import com.unobank.auth_service.database.models.RegisteredCard;
import org.springframework.data.cassandra.repository.CassandraRepository;


public interface RegisteredCardsRepository extends CassandraRepository<RegisteredCard, String> {
}
