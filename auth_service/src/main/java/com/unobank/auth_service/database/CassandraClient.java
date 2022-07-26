package com.unobank.auth_service.database;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.unobank.auth_service.configs.KeyspacesConfig;
import com.unobank.auth_service.database.models.Company;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.EntityWriteResult;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Service;

import static org.springframework.data.cassandra.core.query.Criteria.where;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@Slf4j
public class CassandraClient implements AutoCloseable {
    private CqlSession cqlSession;
    private InsertOptions insertOptions;

    public CassandraClient() throws NoSuchAlgorithmException {
        System.out.println("Enter CassandraClient()");
        // use Java-based bean metadata to register an instance of a com.datastax.oss.driver.api.core.CqlSession
        cqlSession = new KeyspacesConfig().session();
        System.out.println("Initialized session");

        // You can also configure additional options such as TTL, consistency level, and lightweight transactions when using InsertOptions and UpdateOptions
        insertOptions = org.springframework.data.cassandra.core.InsertOptions.builder().
                consistencyLevel(ConsistencyLevel.LOCAL_QUORUM).
                build();
    }

    private static Company addCompany(String companyName, String uniqueBusinessIdentifier) {
        return new Company(UUID.randomUUID().toString(), companyName, uniqueBusinessIdentifier);
    }

    public void insertOne() {
        // The CqlTemplate can be used within a DAO implementation through direct instantiation with a SessionFactory reference or be configured in
        // the Spring container and given to DAOs as a bean reference. CqlTemplate is a foundational building block for CassandraTemplate
        CassandraOperations template = new CassandraTemplate(cqlSession);

        // Let's insert a new Company
        EntityWriteResult<Company> company = template.insert(addCompany("Amazon Inc.", "15-048-3782"), insertOptions);
        // Let's select tne newly inserted company from the Amazon Keyspaces table
        // Place your companyId into the query
        // Select the first record from the table with no where clause
        Company resultOne = template.selectOne(Query.empty().limit(1), Company.class);
        // Select the second record based on the previous query with where clause
        Company resultSecond = template.selectOne(
                Query.query(where("companyId").is(resultOne.getCompanyId())), Company.class);
        log.info(resultSecond.toString());
    }

    @Override
    public void close() throws Exception {
        cqlSession.close();
    }
}
