package com.unobank.auth_service.database.models;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table("students_details")
//@CqlName("students_details")
public class RegisteredCard {
    @PrimaryKey
    private RegisteredCardPrimaryKey cardId;

    @Column("credit_limit")
    @CassandraType(type = Name.INT)
    private int creditLimit;
}
