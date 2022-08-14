package com.unobank.auth_service.database;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.unobank.auth_service.configs.KeyspacesConfig;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import java.security.NoSuchAlgorithmException;

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

    public void insertWithOneArg(String query, String value) {
        log.info("Executing the next query: {}", query);
        // Use a prepared query for quoting
        PreparedStatement prepared = cqlSession.prepare(query);

        // We use execute to send a query to Cassandra. This returns a ResultSet, which is essentially a collection
        // of Row objects.
        ResultSet rs = cqlSession.execute(prepared.bind(value));
        log.info("Query is executed, resultSet:");
    }

    @Override
    public void close() throws Exception {
        cqlSession.close();
    }
}
