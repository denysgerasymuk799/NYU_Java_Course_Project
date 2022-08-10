package com.unobank.orchestrator_service.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic createNgDevTopic() {
        return TopicBuilder.name("TransactionService").partitions(3).replicas(1).build();
    }
}
