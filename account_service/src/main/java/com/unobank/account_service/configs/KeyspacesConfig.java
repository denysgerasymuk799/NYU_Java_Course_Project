// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.unobank.account_service.configs;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.NoSuchAlgorithmException;

@Configuration
public class KeyspacesConfig {
    @Primary
    public @Bean
    CqlSession session() throws NoSuchAlgorithmException {
        File driverConfig = new File("./application.conf");
        Dotenv dotenv = Dotenv
                .configure()
                .directory("./")
                .load();
        String username = dotenv.get("KEYSPACES_USERNAME");
        String password = dotenv.get("KEYSPACES_PASSWORD");

        return CqlSession.builder().
                withConfigLoader(DriverConfigLoader.fromFile(driverConfig)).
                withAuthCredentials(username, password).
                withSslContext(SSLContext.getDefault()).
                withKeyspace("web_banking").
                build();
    }
}
