# NYU_Java_Course_Project


<p align="center">
  <img src="https://user-images.githubusercontent.com/42843889/184997881-3459e0a0-fdf5-4753-b494-7d66858b0728.jpeg" alt="NYU_logo"/>
</p>


## Short Description

This project is a submission for NYU Java course project. It is called **unobank**, which is a web banking project. It allows users to sign up and sign in into their accounts, top up their card balances, send money based on the card_id of recipient, and see a list of transactions. The project's architecture is heavily decoupled by using **five** microservices that interact with each other.

**Project documentation** -- https://proximal-bladder-a8d.notion.site/NYU-Java-unobank-Online-Web-Banking-aa5d609d46e84c59815e79400c2bb4ec


## Features

- Functionality: `Deposit money`, `Send money`, `List transactions`, `Sign in/Sign Up with JWT Token`
- Technologies: `Kafka`, `Java 8`
- Frameworks and Libraries: `Spring Boot`, `Kafka for Spring`, `Kafka Streams`, `Spring Data Cassandra`, `Spring Data Mongo`, `AWS Java SDK`
- Databases: `AWS Keyspaces (serverless Cassandra)`, `Cloud MongoDB`, `AWS S3`
- Java features: `Development of microservices`, `REST API development`, `Interaction with Kafka (event-driven design)`, `Interaction with two NoSQL databases`, `Logic with JWT tokens for authorization`


## Diagrams

## How to run the project

## Useful commands

* Start zookeeper server: `./bin/zookeeper-server-start.sh config/zookeeper.properties`

* Start kafka bootstrap server: `./bin/kafka-server-start.sh config/server.properties`

* Create topic: `./bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic TransactionService --create --partitions 3 --replication-factor 1`

* Use the following command to view messages from the topic in the console: `./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic TransactionService --from-beginning`

* Delete topic: `kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic first_topic`


## Content

* Why do Transaction and Cards services not have JWT validation?


| Port | Microservice |
| --- | --- |
| 8001 | Auth Service |
| 8002 | Orchestrator Service |
| 8003 | Transaction Service |
| 8004 | Card Service |
| 8005 | Account Service |


* http://localhost:8001/swagger-ui/index.html

* http://localhost:8002/swagger-ui/index.html

* http://localhost:8005/swagger-ui/index.html
