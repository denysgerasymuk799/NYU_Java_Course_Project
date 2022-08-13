package com.unobank.card_service.database;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Component;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.unobank.card_service.configs.KeyspacesConfig;

@Component
@Slf4j
public class CassandraClient implements AutoCloseable {
    private CqlSession cqlSession;
    private InsertOptions insertOptions;

    public CassandraClient() throws NoSuchAlgorithmException {
        // use Java-based bean metadata to register an instance of a com.datastax.oss.driver.api.core.CqlSession
        cqlSession = new KeyspacesConfig().session();
        System.out.println("Cassandra session is initialized");

        // You can also configure additional options such as TTL, consistency level,
        // and lightweight transactions when using InsertOptions and UpdateOptions
        insertOptions = org.springframework.data.cassandra.core.InsertOptions.builder().
                consistencyLevel(ConsistencyLevel.QUORUM).
                build();
    }

    public void executeInsertQuery(String query) {
        log.info("Executing the next query: {}", query);

        // We use execute to send a query to Cassandra. This returns a ResultSet, which is essentially a collection
        // of Row objects.
        ResultSet rs = cqlSession.execute(query);
        log.info("Query is executed.");
    }

    public void insertWithOneArg(String query, String value) {
        log.info("Executing the next query: {}", query);
        // Use a prepared query for quoting
        PreparedStatement prepared = cqlSession.prepare(query);

        // We use execute to send a query to Cassandra. This returns a ResultSet, which is essentially a collection
        // of Row objects.
        ResultSet rs = cqlSession.execute(prepared.bind(value));
        log.info("Query is executed.");
    }

    public List<Row> selectWithOneArg(String query, String value) {
        log.info("Executing the next query: {}", query);
        // Use a prepared query for quoting
        PreparedStatement prepared = cqlSession.prepare(query);

        // We use execute to send a query to Cassandra. This returns a ResultSet, which is essentially a collection
        // of Row objects
        ResultSet rs = cqlSession.execute(prepared.bind(value));
        log.info("Query is executed, resultSet:");
        for (Row row : rs) {
            System.out.println(row);
        }

        return rs.all();
    }

    @Override
    public void close() throws Exception {
        cqlSession.close();
    }
}
