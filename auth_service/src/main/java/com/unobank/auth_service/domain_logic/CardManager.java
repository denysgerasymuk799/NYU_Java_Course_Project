package com.unobank.auth_service.domain_logic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import com.unobank.auth_service.database.models.Card;
import com.unobank.auth_service.database.repos.RegistrationCardsRepository;

@Slf4j
public class CardManager {
    private RegistrationCardsRepository mongoCardsRepo;

    public CardManager() {}

    @Autowired
    public CardManager(RegistrationCardsRepository mongoCardsRepo) {
        this.mongoCardsRepo = mongoCardsRepo;
    }

    public Card allocateCard() {
        log.info("Allocating a card for the new user");
        Card card = mongoCardsRepo.findOneByEnabled(false);

        if (card == null) {
            return null;
        }
        log.info("Found available card in db: card id -- {}", card.getCardId());

        // Mark it as taken
        card.setEnabled(true);
        mongoCardsRepo.save(card);

        return card;
    }
}
