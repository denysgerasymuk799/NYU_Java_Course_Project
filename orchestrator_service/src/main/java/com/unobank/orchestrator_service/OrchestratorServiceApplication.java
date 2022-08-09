package com.unobank.orchestrator_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class OrchestratorServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(OrchestratorServiceApplication.class, args);
	}

}
