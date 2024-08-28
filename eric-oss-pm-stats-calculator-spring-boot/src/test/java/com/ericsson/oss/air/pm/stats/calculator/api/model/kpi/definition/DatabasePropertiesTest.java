/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Properties;
import java.util.stream.Stream;

class DatabasePropertiesTest {

    @Test
    void newInstanceWithDatabaseAndProperties() {
        final DatabaseProperties databaseProperties = DatabaseProperties.newInstance(new Database("database"), new Properties());
        Assertions.assertThat(databaseProperties).isNotNull();
    }

    @Test
    void shouldThrowNullPointerException() {
        final Database no_database = new Database("no_database");
        final DatabaseProperties databaseProperties = DatabaseProperties.newInstance(new Database("database"), new Properties());

        Assertions.assertThatThrownBy(() -> databaseProperties.getDatabaseJdbcUrl(no_database))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(String.format("Database '%s' is not found in the '%s'", no_database.getName(), DatabaseProperties.class.getSimpleName()));
    }

    @ParameterizedTest(name = "[{index}] DatabaseProperties: {2} DatabaseProperties: {0} Database: {1}")
    @ArgumentsSource(ProvideDataShouldReturnDatabaseJdbcUrl.class)
    void shouldReturnDatabaseJdbcUrl(DatabaseProperties databaseProperties, Database database, String expected) {
        String result = databaseProperties.getDatabaseJdbcUrl(database);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] DatabaseProperties: {2} DatabaseProperties: {0} Database: {1}")
    @ArgumentsSource(ProvideDataShouldReturnDatabaseProperties.class)
    void shouldReturnDatabaseProperties(DatabaseProperties databaseProperties, Database database, Properties expected) {
        Properties result = databaseProperties.getDatabaseProperties(database);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldComputeIfAbsent() {
        final DatabaseProperties databaseProperties = DatabaseProperties.newInstance();

        final Properties actual = databaseProperties.computeIfAbsent(new Database("database"));

        Assertions.assertThat(actual).isEmpty();
    }

    private static final class ProvideDataShouldReturnDatabaseProperties implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            Properties properties = new Properties();
            properties.setProperty("jdbcUrl", "jdbcUrl");

            Properties expectedProperties = new Properties();
            expectedProperties.setProperty("jdbcUrl", "jdbcUrl");

            Database database = new Database("database");
            return Stream.of(
                    Arguments.of(
                            DatabaseProperties.newInstance(database, properties), database, expectedProperties
                    )
            );
        }
    }

    private static final class ProvideDataShouldReturnDatabaseJdbcUrl implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            Database database = new Database("database");
            Properties properties = new Properties();
            properties.setProperty("jdbcUrl", "jdbcUrl");

            return Stream.of(
                    Arguments.of(
                            DatabaseProperties.newInstance(database, new Properties()), database, null
                    ),
                    Arguments.of(
                            DatabaseProperties.newInstance(database, properties), database, "jdbcUrl"
                    )
            );
        }
    }
}
