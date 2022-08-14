package com.unobank.orchestrator_service.domain_logic;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

@Component
public final class Constants {
    public static final Dotenv dotenv = Dotenv
                        .configure()
                        .directory("./")
                        .load();
    public static final String TRANSACTIONS_TOPIC = dotenv.get("TRANSACTIONS_TOPIC");
    public static final String CARDS_TOPIC = dotenv.get("CARDS_TOPIC");
    public static final String ALL_RESULTS_TOPIC = dotenv.get("ALL_RESULTS_TOPIC");
    public static final String KAFKA_CONSUMER_GROUP = dotenv.get("KAFKA_CONSUMER_GROUP");

    // ------------- Message Body Variables --------------
    public static final int RESPONSE_SUCCESS = 200;
    public static final String MESSAGE_TYPE_RESPONSE = "Response";
    public static final String MESSAGE_TYPE_REQUEST = "Request";

    // ---------------- Logger Constants -----------------
    public static final String TRANSACTION_SERVICE_PRODUCER_NAME = "TransactionServiceProducer";
}
