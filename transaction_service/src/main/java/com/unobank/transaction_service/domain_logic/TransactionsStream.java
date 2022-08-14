package com.unobank.transaction_service.domain_logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.transaction_service.domain_logic.enums.TransactionStatus;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.unobank.transaction_service.domain_logic.enums.Events;
import com.unobank.transaction_service.dto.ProcessingTransactionMessage;

import java.util.ArrayList;
import java.util.Map;

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

	/**
	 * Entrypoint method for Kafka Streams. Read messages from TransactionService's topic,
	 * process them and save results in Results topic.
	 * @param builder: default StreamsBuilder argument for the main Kafka Streams method
	 * @return: based on methodology return sourceStream
	 */
	@Bean
	public KStream<String, String> kstreamProcessingTransactions(StreamsBuilder builder) {
		Serde<String> stringSerde = Serdes.String();
		// Read a source stream, filter it from nulls and generate a new stream with processed transactions
		KStream<String, String> sourceStream = builder.stream(this.transactionsTopic, Consumed.with(stringSerde, stringSerde));
		KStream<String, String> filteredStream = sourceStream.filter((k, v) -> v != null);

		// Split the main topic stream on two branches:
		// 		the first one -- for processing transactions;
		// 		the second one -- for results of processed transactions;
		Map<String, KStream<String, String>> branches  = filteredStream.split()
				.branch((k, v) -> Utils.isProcessingTransaction(v))
				.branch((k, v) -> Utils.isResult(v))
				.noDefaultBranch();
		log.info("branches.keySet(): {}", branches.keySet());

		// Setup pipeline for processing new transactions
		ArrayList<String> mapKeys = new ArrayList<>(branches.keySet());
		KStream<String, String> processedTransactionsStream  = branches.get(mapKeys.get(0)).mapValues(this::processTransaction);
		// Filter nulls in case of errors
		KStream<String, String> resultTransactionsFilteredStream = processedTransactionsStream.filter((k, v) -> v != null);

		// Setup pipeline for processed transactions
		KStream<String, String> resultsStream = branches.get(mapKeys.get(1)).mapValues(this::processResults);
		KStream<String, String> resultsFilteredStream = resultsStream.filter((k, v) -> v != null);

		// Send result messages in appropriate topics
		resultTransactionsFilteredStream.to(cardsTopic);
		resultsFilteredStream.to(resultsTopic);

		// Display each processed message
		sourceStream.print(Printed.<String, String>toSysOut().withLabel("JSON original stream"));
		resultTransactionsFilteredStream.print(Printed.<String, String>toSysOut().withLabel("JSON processTransaction stream"));
		resultsFilteredStream.print(Printed.<String, String>toSysOut().withLabel("JSON processResults stream"));

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

			// Do event-based processing
			ProcessingTransactionMessage messageForCardService = null;
			if (transaction.getEventName().equals(Events.TRANSACTION_TOPUP.label)) {
				messageForCardService = transactionService.createTopupTransaction(transaction);
			} else if (transaction.getEventName().equals(Events.TRANSACTION_REQUEST.label)) {
				messageForCardService = transactionService.createTransaction(transaction);
			} else if (transaction.getEventName().equals(Events.RESERVATION_SUCCESS.label)) {
				messageForCardService = transactionService.executeTransaction(transaction);
			}
			log.info("Processed the transaction: [{}]. Event: {}.", transaction.getData().getTransactionId(), transaction.getEventName());
			return outputObjectMapper.writeValueAsString(messageForCardService);
		} catch (Exception e) {
			log.error(e.toString());
			return null;
		}
	}

	/**
	 * Process a message from TransactionService's topic and send a response to Results topic for Orchestrator service.
	 * @param message: JSON in string format, which contains transaction info.
	 */
	public String processResults(String message) {
		try {
			ProcessingTransactionMessage transaction = inputObjectMapper.readValue(message, ProcessingTransactionMessage.class);
			System.out.println("transaction: " + transaction);
			log.info("Start processing of a new result transaction: [{}]. Event: {}.", transaction.getData().getTransactionId(), transaction.getEventName());

			// Do event-based processing.
			// NOTE that in this method we process just TRANSACTION_SUCCESS and TRANSACTION_FAILURE,
			// since other events are processed in processTransaction method
			ProcessingTransactionMessage messageForCardService = null;
			if (transaction.getEventName().equals(Events.TRANSACTION_SUCCESS.label)) {
				messageForCardService = transactionService.setTransactionCompletionStatus(transaction, TransactionStatus.COMPLETED);
			} else if (transaction.getEventName().equals(Events.TRANSACTION_FAILURE.label)) {
				messageForCardService = transactionService.setTransactionCompletionStatus(transaction, TransactionStatus.FAILED);
			} else if (transaction.getEventName().equals(Events.RESERVATION_FAILURE.label)) {
				messageForCardService = transactionService.setTransactionCompletionStatus(transaction, TransactionStatus.FAILED);
			}
			log.info("Completed the transaction: [{}]. Event: {}.", transaction.getData().getTransactionId(), transaction.getEventName());
			return outputObjectMapper.writeValueAsString(messageForCardService);
		} catch (Exception e) {
			log.error(e.toString());
			return null;
		}
	}
}
