// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.unobank.auth_service.configs;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import io.github.cdimascio.dotenv.Dotenv;

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
