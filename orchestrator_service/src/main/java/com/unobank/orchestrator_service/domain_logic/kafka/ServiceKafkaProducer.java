package com.unobank.orchestrator_service.domain_logic.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.unobank.orchestrator_service.domain_logic.Constants;
import com.unobank.orchestrator_service.dto.TransactionMessage;

import java.util.UUID;

@Slf4j
@Component
public class ServiceKafkaProducer {
	private final String producerName = Constants.TRANSACTION_SERVICE_PRODUCER_NAME;
	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	ObjectMapper objectMapper;

	public void processTransaction(TransactionMessage transactionMessage) throws JsonProcessingException {
		transactionMessage.setProducer(producerName);
		String key = (transactionMessage.getData().getReceiverCardId() == null) ? UUID.randomUUID().toString() : transactionMessage.getData().getReceiverCardId();
		String value = objectMapper.writeValueAsString(transactionMessage);

		assert Constants.TRANSACTIONS_TOPIC != null;
		ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send(Constants.TRANSACTIONS_TOPIC, key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}

			@Override
			public void onSuccess(SendResult<String, String> result) {
				handleSuccess(key, value, result);
			}
		});
	}

	private void handleFailure(String key, String value, Throwable ex) {
		log.error("Error while Sending the Message and the exception is {}", ex.getMessage());
		try {
			throw ex;
		} catch (Throwable throwable) {
			log.error("Error in OnFailure listenable callback : {}", throwable.getMessage());
		}

	}

	private void handleSuccess(String key, String value, SendResult<String, String> result) {
		log.info("Message Sent SuccessFully. key : {} and the value is {} , partition used is {}", key, value,
				result.getRecordMetadata().partition());
	}
}
