# NYU_Java_Course_Project


## Useful commands

* Start zookeeper server: `./bin/zookeeper-server-start.sh config/zookeeper.properties`

* Start kafka bootstrap server: `./bin/kafka-server-start.sh config/server.properties`

* Create topic: `./bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic TransactionService --create --partitions 3 --replication-factor 1`

* Use the following command to view messages from the topic in the console: `./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic TransactionService --from-beginning`


## Content

* Why do Transaction and Cards services not have JWT validation?
