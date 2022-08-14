package com.unobank.orchestrator_service.domain_logic.kafka;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import java.util.Objects;

import com.unobank.orchestrator_service.domain_logic.enums.TransactionType;
import com.unobank.orchestrator_service.dto.ProcessingTransactionMessage;

@Slf4j
@Service
public class ResultsConsumer {
    @Autowired
    private ObjectMapper objectMapper;

    private final Dotenv dotenv = Dotenv
            .configure()
            .directory("./")
            .load();

    private final AWSCredentials credentials = new BasicAWSCredentials(
            Objects.requireNonNull(dotenv.get("S3_ACCESS_KEY")),
            Objects.requireNonNull(dotenv.get("S3_SECRET_KEY"))
    );

    private final String BUCKET_NAME =  dotenv.get("BUCKET_NAME");

    private final AmazonS3 s3Client = AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.EU_CENTRAL_1)
            .build();

    @RetryableTopic(
            autoCreateTopics = "true", attempts = "3",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            backoff = @Backoff(delay = 3000, maxDelay = 10_000, multiplier = 1.5, random = true),
            dltTopicSuffix = "-dead"
    )
    @KafkaListener(topics = "#{constants.ALL_RESULTS_TOPIC}", concurrency = "2")
    public void consumeResults(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        ProcessingTransactionMessage message = objectMapper.readValue(consumerRecord.value(), ProcessingTransactionMessage.class);
        log.info("Processing on partition {} for a transaction result {}", consumerRecord.partition(), message);

        String content = objectMapper.writeValueAsString(message);
        // Save the transaction result in senderCardId folder
        String key = String.format("%s/%s/%s.json",
                message.getData().getSenderCardId(), message.getData().getDate(), message.getData().getTransactionId());
        PutObjectResult res = s3Client.putObject(BUCKET_NAME, key, content);
        log.info("Record put into sender S3 folder: {}", res.toString());

        if (!message.getData().getReceiverCardId().equals(TransactionType.TOP_UP.toString())) {
            // Save the transaction result in receiverCardId folder
            key = String.format("%s/%s/%s.json",
                    message.getData().getReceiverCardId(), message.getData().getDate(), message.getData().getTransactionId());
            res = s3Client.putObject(BUCKET_NAME, key, content);
            log.info("Record put into receiver S3 folder: {}", res);
        }
    }
}
