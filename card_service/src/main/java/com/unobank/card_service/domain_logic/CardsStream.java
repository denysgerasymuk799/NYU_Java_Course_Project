package com.unobank.card_service.domain_logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.card_service.domain_logic.enums.TransactionStatus;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.unobank.card_service.domain_logic.enums.Events;
import com.unobank.card_service.dto.ProcessingTransactionMessage;
import com.unobank.card_service.dto.TransactionMessage;

@Slf4j
@Configuration
public class CardsStream {
	@Autowired
	private CardService cardService;
	private final ObjectMapper inputObjectMapper = new ObjectMapper();
	private final ObjectMapper outputObjectMapper = new ObjectMapper();
	private final Dotenv dotenv = Dotenv
			.configure()
			.directory("./")
			.load();
	private final String transactionsTopic = dotenv.get("TRANSACTIONS_TOPIC");
	private final String cardsTopic = dotenv.get("CARDS_TOPIC");
	private final String resultsTopic = dotenv.get("ALL_RESULTS_TOPIC");

	@Bean
	public KStream<String, String> kstreamProcessingTransactions(StreamsBuilder builder) {
		Serde<String> stringSerde = Serdes.String();
		KStream<String, String> sourceStream = builder.stream(this.cardsTopic, Consumed.with(stringSerde, stringSerde));
		KStream<String, String> filteredStream = sourceStream.filter((k, v) -> v != null);
		KStream<String, String> uppercaseStream = filteredStream.mapValues(this::processTransaction);

		uppercaseStream.to(transactionsTopic);
		sourceStream.print(Printed.<String, String>toSysOut().withLabel("JSON original stream"));
		uppercaseStream.print(Printed.<String, String>toSysOut().withLabel("JSON processTransaction stream"));

		return sourceStream;
	}

	/**
	 * Process a message from CardService's topic and send a response to TransactionService's topic.
	 * @param message: JSON in string format, which contains transaction info.
	 */
	public String processTransaction(String message) {
		try {
			ProcessingTransactionMessage transaction = inputObjectMapper.readValue(message, ProcessingTransactionMessage.class);
			System.out.println("transaction: " + transaction);
			log.info("Start processing of a new transaction: [{}]. Event: {}.", transaction.getData().getTransactionId(), transaction.getEventName());

			// Do event-based processing
			ProcessingTransactionMessage messageForTransactionService = null;
			if (transaction.getEventName().equals(Events.TRANSACTION_CREATED.label)) {
				messageForTransactionService = cardService.reserveBalance(transaction);
			} else if (transaction.getEventName().equals(Events.TRANSACTION_PENDING.label)) {
				messageForTransactionService = cardService.processPayment(transaction);
			} else if (transaction.getEventName().equals(Events.RESERVATION_CANCEL.label)) {
				messageForTransactionService = cardService.cancelReservation(transaction);
			}
			if (messageForTransactionService != null) {
				log.info("Completed the transaction: [{}]. Event: {}.", transaction.getData().getTransactionId(), transaction.getEventName());
				return outputObjectMapper.writeValueAsString(messageForTransactionService);
			}
			return null;
		} catch (Exception e) {
			log.error(e.toString());
			return "";
		}
	}
}
