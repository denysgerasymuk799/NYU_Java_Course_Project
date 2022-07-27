package com.unobank.auth_service.domain_logic;

import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.unobank.auth_service.database.CassandraClient;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import com.unobank.auth_service.database.models.Card;
import com.unobank.auth_service.database.repos.RegistrationCardsRepository;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
@Slf4j
public class CardManager {
    private RegistrationCardsRepository mongoCardsRepo;
    private CassandraClient cassandraClient;
    private Dotenv dotenv;
    private String cardTable;
    private String unique_users_daily;

    public CardManager() {}

    @Autowired
    public CardManager(RegistrationCardsRepository mongoCardsRepo) throws NoSuchAlgorithmException {
        this.mongoCardsRepo = mongoCardsRepo;
        this.cassandraClient = new CassandraClient();
        this.dotenv = Dotenv
                        .configure()
                        .directory("./")
                        .load();
        this.cardTable = dotenv.get("CARD_TABLE");
        this.unique_users_daily = dotenv.get("USERS_UNIQUE_DAILY_TABLE");
    }

    private Card allocateCard() {
        log.info("Allocating a card for the new user ...");
        Card card = mongoCardsRepo.findOneByEnabled(false).findFirst().orElse(null);

        if (card == null) {
            return null;
        }
        log.info("Found an available card in db: card id -- {}", card.getCardId());

        // Mark it as taken
        card.setEnabled(true);
        mongoCardsRepo.save(card);

        return card;
    }

    private String getCurrentDate() {
        Instant instant = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));
        return formatter.format(instant);
    }

    public String assignCard() throws InvalidQueryException {
        String cardId = Objects.requireNonNull(allocateCard()).getCardId();
        String query = String.format("INSERT INTO %s (card_id, credit_limit) VALUES (?, 500)", cardTable);
        cassandraClient.insertOne(query, cardId);

        query = String.format("INSERT INTO %s (card_id, date) VALUES (?, '%s');", unique_users_daily, getCurrentDate());
        cassandraClient.insertOne(query, cardId);

        return cardId;
    }
}
