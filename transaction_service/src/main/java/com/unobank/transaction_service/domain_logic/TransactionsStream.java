package com.unobank.transaction_service.domain_logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.transaction_service.domain_logic.enums.TransactionStatus;
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

import com.unobank.transaction_service.domain_logic.enums.Events;
import com.unobank.transaction_service.dto.ProcessingTransactionMessage;
import com.unobank.transaction_service.dto.TransactionMessage;

@Slf4j
@Configuration
public class TransactionsStream {
	@Autowired
	private TransactionService transactionService;
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
		KStream<String, String> sourceStream = builder.stream(this.transactionsTopic, Consumed.with(stringSerde, stringSerde));
		KStream<String, String> filteredStream = sourceStream.filter((k, v) -> v != null);
		KStream<String, String> uppercaseStream = filteredStream.mapValues(this::processTransaction);

		uppercaseStream.to(cardsTopic);
		sourceStream.print(Printed.<String, String>toSysOut().withLabel("JSON original stream"));
		uppercaseStream.print(Printed.<String, String>toSysOut().withLabel("JSON processTransaction stream"));

		return sourceStream;
	}

	/**
	 * Process a message from TransactionService's topic and send a response to CardService's topic.
	 * @param message: JSON in string format, which contains transaction info.
	 */
	public String processTransaction(String message) {
		try {
			ProcessingTransactionMessage transaction = inputObjectMapper.readValue(message, ProcessingTransactionMessage.class);
			System.out.println("transaction: " + transaction);
			log.info("Start processing of a new transaction: [{}]. Event: {}.", transaction.getData().getTransactionId(), transaction.getEventName());

			ProcessingTransactionMessage messageForCardService;
			if (transaction.getEventName().equals(Events.TRANSACTION_REQUEST.label)) {
				messageForCardService = transactionService.createTransaction(transaction);
			} else if (transaction.getEventName().equals(Events.RESERVATION_SUCCESS.label)) {
				messageForCardService = transactionService.executeTransaction(transaction);
			} else if (transaction.getEventName().equals(Events.TRANSACTION_SUCCESS.label)) {
				messageForCardService = transactionService.setTransactionCompletionStatus(transaction, TransactionStatus.COMPLETED);
			} else {
				messageForCardService = transactionService.setTransactionCompletionStatus(transaction, TransactionStatus.FAILED);
			}
			log.info("Completed the transaction: [{}]. Event: {}.", transaction.getData().getTransactionId(), transaction.getEventName());
			return outputObjectMapper.writeValueAsString(messageForCardService);
		} catch (Exception e) {
			log.error(e.toString());
			return "";
		}
	}
}
