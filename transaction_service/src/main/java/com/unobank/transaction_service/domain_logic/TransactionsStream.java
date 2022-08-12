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
	private ObjectMapper inputObjectMapper = new ObjectMapper();
	private ObjectMapper outputObjectMapper = new ObjectMapper();
	private Dotenv dotenv = Dotenv
			.configure()
			.directory("./")
			.load();
	private String transactionsTopic = dotenv.get("TRANSACTIONS_TOPIC");
	private String cardsTopic = dotenv.get("CARDS_TOPIC");
	private String resultsTopic = dotenv.get("ALL_RESULTS_TOPIC");

	@Bean
	public KStream<String, String> kstreamProcessingTransactions(StreamsBuilder builder) {
		Serde<String> stringSerde = Serdes.String();
		KStream<String, String> sourceStream = builder.stream(this.transactionsTopic, Consumed.with(stringSerde, stringSerde));
		KStream<String, String> uppercaseStream = sourceStream.mapValues(this::processTransaction);

		uppercaseStream.to(cardsTopic);
		sourceStream.print(Printed.<String, String>toSysOut().withLabel("JSON original stream"));
		uppercaseStream.print(Printed.<String, String>toSysOut().withLabel("JSON processTransaction stream"));

		return sourceStream;
	}

	public String processTransaction(String message) {
		try {
			TransactionMessage transaction = inputObjectMapper.readValue(message, TransactionMessage.class);
			System.out.println("transaction: " + transaction);
			log.info("Start a new transaction: [{}]. Event: {}.", transaction.getData().getTransactionId(), transaction.getEventName());

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
			System.out.println(e.toString());
			return "";
		}
	}
}
