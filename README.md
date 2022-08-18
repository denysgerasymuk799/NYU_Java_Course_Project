# NYU_Java_Course_Project


<p align="center">
  <img src="https://user-images.githubusercontent.com/42843889/184997881-3459e0a0-fdf5-4753-b494-7d66858b0728.jpeg" alt="NYU_logo"/>
</p>


## Short Description

This project is a submission for NYU Java course project. It is called **unobank**, which is a web banking project. It allows users to sign up and sign in into their accounts, top up their card balances, send money based on the card_id of recipient, and see a list of transactions. The project's architecture is heavily decoupled by using **five** microservices that interact with each other.

**Project documentation** -- https://proximal-bladder-a8d.notion.site/NYU-Java-unobank-Online-Web-Banking-aa5d609d46e84c59815e79400c2bb4ec

Port to microservice table:

| Port | Microservice |
| --- | --- |
| 8001 | Auth Service |
| 8002 | Orchestrator Service |
| 8003 | Transaction Service |
| 8004 | Card Service |
| 8005 | Account Service |


You can access Swagger documentation following these links:

* http://localhost:8001/swagger-ui/index.html

* http://localhost:8002/swagger-ui/index.html

* http://localhost:8005/swagger-ui/index.html


## Features

- Functionality: `Deposit money`, `Send money`, `List transactions`, `Sign in/Sign Up with JWT Token`
- Technologies: `Kafka`, `Java 8`
- Frameworks and Libraries: `Spring Boot`, `Kafka for Spring`, `Kafka Streams`, `Spring Data Cassandra`, `Spring Data Mongo`, `AWS Java SDK`
- Databases: `AWS Keyspaces (serverless Cassandra)`, `Cloud MongoDB`, `AWS S3`
- Java features: `Development of microservices`, `REST API development`, `Interaction with Kafka (event-driven design)`, `Interaction with two NoSQL databases`, `Logic with JWT tokens for authorization`


## Diagrams

The high-level diagram of my services from the infrastructure side looks like this:  

<p align="center">
  <img src="https://user-images.githubusercontent.com/42843889/185481242-3e31d911-3582-4380-9b9e-f48491a92617.png" alt="NYU_Java_project_architecture_v2"/>
</p>


<pre>


</pre>


The high-level diagram of my services from the interaction side looks like this:  

<p align="center">
  <img src="https://user-images.githubusercontent.com/42843889/185481150-ec520b99-e535-4984-a2b7-4f9fe71857ed.png" alt="service_integration_v2"/>
</p>


<pre>


</pre>


Transaction processing in more detail:  

<p align="center">
  <img src="https://user-images.githubusercontent.com/25267308/170844977-67ba2bec-4c75-48ab-bca6-a7b775ef2b24.svg" alt="transactions"/>
</p>


## How to run the project

1) Download Kafka [from here](https://kafka.apache.org/downloads) and unzip in any folder. Commands to run Kafka locally:
```shell
# Assuming the root is a unzipped Kafka folder
$ ./bin/zookeeper-server-start.sh config/zookeeper.properties

$ ./bin/kafka-server-start.sh config/server.properties

# Create required Kafka topics
$ ./bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic TransactionService --create --partitions 3 --replication-factor 1

$ ./bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic CardService --create --partitions 3 --replication-factor 1

$ ./bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic ResultsTopic --create --partitions 3 --replication-factor 1
```

2) Open all microservices in separate console windows or in separate your IDE windows. Example with a console:
```shell
NYU_Java_Course_Project/auth_service$ bash run_service.sh

NYU_Java_Course_Project/orchestrator_service$ bash run_service.sh

NYU_Java_Course_Project/transaction_service$ bash run_service.sh

NYU_Java_Course_Project/card_service$ bash run_service.sh

NYU_Java_Course_Project/account_service$ bash run_service.sh
```

3) Download `npm` for your operation system. Type the following commands to run the React UI:
```shell
$ npm install

$ npm start
```

For more detailes, read README in `./bank_react_web`.

4) For API testing you can use a Postman collection in `./docs/NYU_Java_Project.postman_collection.json`. First, you must register or use already existing credentials for login to get JWT token in the response and use it in a Authorization header for any other endpoints. Note that JWT will expire in **20 minutes**. For more details you can follow my above youtube video.

5) Enjoy the React UI and explore Swagger docuementation:

* http://localhost:8001/swagger-ui/index.html

* http://localhost:8002/swagger-ui/index.html

* http://localhost:8005/swagger-ui/index.html


## Useful commands

* Start zookeeper server: `./bin/zookeeper-server-start.sh config/zookeeper.properties`

* Start kafka bootstrap server: `./bin/kafka-server-start.sh config/server.properties`

* Create topic: `./bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic TransactionService --create --partitions 3 --replication-factor 1`

* Use the following command to view messages from the topic in the console: `./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic TransactionService --from-beginning`

* Delete topic: `kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic first_topic`
