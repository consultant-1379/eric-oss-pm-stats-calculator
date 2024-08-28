/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.datasource;

import java.sql.Connection;
import java.util.Properties;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

class JdbcDatasourceTest {
    EmbeddedDatabase embeddedDatabase;

    @BeforeEach
    void setUp() {
        embeddedDatabase = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Test
    void shouldGetConnection() throws Exception {
        try (final Connection connection = embeddedDatabase.getConnection()) {
            final String jbdcConnection = connection.getMetaData().getURL();

            final Properties jdbcProperties = new Properties();
            jdbcProperties.setProperty("user", "SA");
            jdbcProperties.setProperty("password", "");

            final JdbcDatasource jdbcDatasource = new JdbcDatasource(jbdcConnection, jdbcProperties);

            final Connection actual = jdbcDatasource.getConnection();

            Assertions.assertThat(actual).isNotNull();
        }
    }

    @Test
    void shouldGetDatasource() {
        final Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty("expressionTag", "datasource");

        final JdbcDatasource jdbcDatasource = new JdbcDatasource("connection", jdbcProperties);

        Assertions.assertThat(jdbcDatasource.getDatasource()).isEqualTo(Datasource.of("datasource"));
    }
}