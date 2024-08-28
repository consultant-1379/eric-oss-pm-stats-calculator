/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlAppenderImpl;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlTableGeneratorImpl;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlTableModifierImpl;
import com.ericsson.oss.air.pm.stats.test_utils.DatabasePropertiesMock;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@ExtendWith(MockitoExtension.class)
class DatabaseRepositoryImplTest {
    private static final String DATABASE_NAME = "kpi_service_db";
    private static final String DATABASE_URL = String.format("jdbc:h2:mem:%s", DATABASE_NAME);
    private static final String NON_EXISTING_JDBC_URL = "jdbc:postgresql://localhost:5432/nonExisting";

    private EmbeddedDatabase embeddedDatabase;

    @InjectMocks
    SqlAppenderImpl sqlAppenderSpy = spy(new SqlAppenderImpl());
    @InjectMocks
    SqlTableGeneratorImpl sqlTableGeneratorSpy = spy(new SqlTableGeneratorImpl());
    @InjectMocks
    SqlTableModifierImpl sqlTableModifierSpy = spy(new SqlTableModifierImpl());

    @InjectMocks
    DatabaseRepositoryImpl objectUnderTest;

    @BeforeEach
    void setUp() {
        final EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder();
        embeddedDatabase = embeddedDatabaseBuilder.setType(EmbeddedDatabaseType.H2)
                .setName(String.format("%s;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE", DATABASE_NAME))
                .addScript("sql/initialize_kpi_definition.sql")
                .build();
    }

    @AfterEach
    void tearDown() {
        embeddedDatabase.shutdown();
    }

    @MethodSource("provideIsAvailableData")
    @ParameterizedTest(name = "[{index}] Database is available: ''{2}'' when JDBC URL: ''{0}'' and Properties: ''{1}''")
    void shouldVerifyIsAvailable(final String jdbcUrl, final Properties properties, final boolean expected) {
        DatabasePropertiesMock.prepare(jdbcUrl, properties, () -> {
            final boolean actual = objectUnderTest.isAvailable();

            Assertions.assertThat(actual).isEqualTo(expected);
        });
    }

    @MethodSource("provideDoesTableExistByNameData")
    @ParameterizedTest(name = "[{index}] Table: ''{0}'' exists: ''{1}''")
    void shouldVerifyDoesTableExistByName(final String name, final boolean expected) {
        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final boolean actual = objectUnderTest.doesTableExistByName(name);

            Assertions.assertThat(actual).isEqualTo(expected);
        });
    }

    @Test
    void shouldFindAllCalculationOutputTables() {
        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final String randomSchema = "random_schema";
            final String kpiSchema = "kpi";

            createSchema(randomSchema);
            createTable(randomSchema, "random_table");
            createTable(randomSchema, "kpi_cell_sector_1440");
            createTable(kpiSchema, "kpi_cell_sector");
            createTable(kpiSchema, "kpi_cell_sector_60");
            createTable(kpiSchema, "kpi_definition");
            createTable(kpiSchema, "kpi_calculation");

            final List<String> actual = objectUnderTest.findAllCalculationOutputTables();

            Assertions.assertThat(actual).containsExactlyInAnyOrder("kpi_cell_sector", "kpi_cell_sector_60");
        });
    }

    @Nested
    @DisplayName("should throw UncheckedSqlException when something goes wrong")
    class ShouldThrowUncheckedSqlExceptionWhenSomethingGoesWrong {
        static final String errorMessage = "Something went wrong...";
        final SQLException sqlException = new SQLException(errorMessage);

        @Test
        void onDoesTableExistByName() {
            DriverManagerMock.prepare(connectionMock -> {
                when(connectionMock.getMetaData()).thenThrow(sqlException);

                Assertions.assertThatThrownBy(() -> objectUnderTest.doesTableExistByName("tableName"))
                        .hasRootCauseInstanceOf(SQLException.class)
                        .hasRootCauseMessage(errorMessage)
                        .isInstanceOf(UncheckedSqlException.class);

                verify(connectionMock).getMetaData();
            });
        }

        @Test
        void onFindAllCalculationOutputTables() {
            DriverManagerMock.prepare(connectionMock -> {
                when(connectionMock.createStatement()).thenThrow(sqlException);

                Assertions.assertThatThrownBy(() -> objectUnderTest.findAllCalculationOutputTables())
                        .hasRootCauseInstanceOf(SQLException.class)
                        .hasRootCauseMessage(errorMessage)
                        .isInstanceOf(UncheckedSqlException.class);

                verify(connectionMock).createStatement();
            });
        }

        @Test
        void onFindAllPrimaryKeys() {
            DriverManagerMock.prepare(connectionMock -> {
                when(connectionMock.getMetaData()).thenThrow(sqlException);

                Assertions.assertThatThrownBy(() -> objectUnderTest.findAllPrimaryKeys(Table.of("table")))
                        .hasRootCauseInstanceOf(SQLException.class)
                        .hasRootCauseMessage(errorMessage)
                        .isInstanceOf(UncheckedSqlException.class);

                verify(connectionMock).getMetaData();
            });
        }

        @Test
        void onReadColumnDefinitions() {
            DriverManagerMock.prepare(connectionMock -> {
                when(connectionMock.getMetaData()).thenThrow(sqlException);

                Assertions.assertThatThrownBy(() -> objectUnderTest.findAllColumnDefinitions(Table.of("table")))
                        .hasRootCauseInstanceOf(SQLException.class)
                        .hasRootCauseMessage(errorMessage)
                        .isInstanceOf(UncheckedSqlException.class);

                verify(connectionMock).getMetaData();
            });
        }
    }

    private static Stream<Arguments> provideDoesTableExistByNameData() {
        return Stream.of(Arguments.of("kpi_definition", true),
                Arguments.of("non_existing_table", false));
    }

    private static Stream<Arguments> provideIsAvailableData() {
        final Properties properties = new Properties();
        properties.setProperty("user", "SA");

        return Stream.of(Arguments.of(NON_EXISTING_JDBC_URL, new Properties(), false),
                Arguments.of(DATABASE_URL, properties, true));
    }

    public void createSchema(final String schema) throws SQLException {
        final String sql = String.format("CREATE SCHEMA IF NOT EXISTS %s", schema);
        try (final Connection connection = embeddedDatabase.getConnection();
             final Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public void createTable(final String schema, final String table) throws SQLException {
        final String sql = String.format("CREATE TABLE IF NOT EXISTS %s.%s ()", schema, table);
        try (final Connection connection = embeddedDatabase.getConnection();
             final Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

}