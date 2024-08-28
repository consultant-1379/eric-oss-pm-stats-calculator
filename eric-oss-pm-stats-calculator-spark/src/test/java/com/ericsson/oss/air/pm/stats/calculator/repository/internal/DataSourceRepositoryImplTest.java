/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import static com.ericsson.oss.air.pm.stats.calculator.repository._util.RepositoryHelpers.database;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@ExtendWith(MockitoExtension.class)
class DataSourceRepositoryImplTest {
    @Mock DatasourceRegistry datasourceRegistryMock;
    @Mock SparkService sparkServiceMock;

    @InjectMocks DataSourceRepositoryImpl objectUnderTest;

    @Nested
    @DisplayName("findUtcTimestamp")
    class FindUtcTimestamp {
        Database h2 = new Database("h2");
        Table dummyTable = Table.of("dummy_table");

        EmbeddedDatabase embeddedDatabase = database("database/h2/initial_table.sql");

        @BeforeEach
        void setUp() throws SQLException {
            when(sparkServiceMock.connectionTo(h2)).thenReturn(embeddedDatabase.getConnection());
        }

        @Test
        void shouldFindNothing_whenFindMinUtcTimestamp() {
            final Optional<Timestamp> actual = objectUnderTest.findAggregationBeginTime(h2, Table.of("random_table"));
            Assertions.assertThat(actual).isEmpty();
        }

        @Test
        void shouldFindMinAggregationBeginTime() {
            final Optional<Timestamp> actual = objectUnderTest.findAggregationBeginTime(h2, dummyTable);
            Assertions.assertThat(actual).hasValue(Timestamp.valueOf("2022-08-03 15:00:00.0"));
        }

        @Test
        void shouldFindMaxAggregationBeginTime() {
            final Optional<Timestamp> actual = objectUnderTest.findMaxAggregationBeginTime(h2, dummyTable);
            Assertions.assertThat(actual).hasValue(Timestamp.valueOf("2022-08-03 17:00:00.0"));
        }
    }

    @Nested
    @DisplayName("isAvailable")
    class IsAvailable {

        EmbeddedDatabase embeddedDatabase;
        @BeforeEach
        void setUp() {
            embeddedDatabase = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
        }

        @Test
        void shouldReturnFalse_whenDatasourceIsNotAvailable() {
            final Datasource datasource = Datasource.of("unavailable");

            when(datasourceRegistryMock.getJdbcDatasource(datasource)).thenReturn(new JdbcDatasource(
                    "jdbc:postgresql://randomHost:5432/",
                    new Properties()
            ));

            final boolean actual = objectUnderTest.isAvailable(datasource);

            verify(datasourceRegistryMock).getJdbcDatasource(datasource);

            Assertions.assertThat(actual).isFalse();
        }

        @Test
        void shouldReturnTrue_whenDatasourceIsAvailable() throws Exception {
            final Datasource datasource = Datasource.of("available");

            final JdbcDatasource jdbcDatasourceMock = mock(JdbcDatasource.class);

            when(datasourceRegistryMock.getJdbcDatasource(datasource)).thenReturn(jdbcDatasourceMock);
            when(jdbcDatasourceMock.getConnection()).thenReturn(embeddedDatabase.getConnection());

            final boolean actual = objectUnderTest.isAvailable(datasource);

            verify(datasourceRegistryMock).getJdbcDatasource(datasource);

            Assertions.assertThat(actual).isTrue();
        }

    }
    @Nested
    @DisplayName("doesTableContainData")
    class DoesTableContainData {

        EmbeddedDatabase embeddedDatabase;
        @Mock JdbcDatasource jdbcDatasourceMock;

        @BeforeEach
        void setUp() {
            embeddedDatabase = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                                                            .addScript("database/init_table.sql")
                                                            .build();
        }

        @MethodSource("com.ericsson.oss.air.pm.stats.calculator.repository.internal.DataSourceRepositoryImplTest#provideDoesTableContainData")
        @ParameterizedTest(name = "[{index}] Contains data between [''{0}'' - ''{1}''] ==> ''{2}''")
        void shouldReturnExpectedValue(final LocalDateTime start, final LocalDateTime end, final boolean expected) throws Exception {
            when(jdbcDatasourceMock.getConnection()).thenReturn(embeddedDatabase.getConnection());

            final boolean actual = objectUnderTest.doesTableContainData(jdbcDatasourceMock, "external_datasource", start, end);

            verify(jdbcDatasourceMock).getConnection();

            Assertions.assertThat(actual).isEqualTo(expected);
        }

        @Test
        void shouldReturnFalse_whenDataSourceIsNotAvailable() throws Exception {
            final LocalDateTime now = LocalDateTime.now();

            doThrow(new SQLException("Invalid connection")).when(jdbcDatasourceMock).getConnection();

            final boolean actual = objectUnderTest.doesTableContainData(jdbcDatasourceMock, "external_datasource", now, now.plusMinutes(1));

            verify(jdbcDatasourceMock).getConnection();

            Assertions.assertThat(actual).isFalse();
        }


    }

    static Stream<Arguments> provideDoesTableContainData() {
        return Stream.of(
                Arguments.of(
                        LocalDateTime.of(2_022, Month.JULY, 14, 8, 0),
                        LocalDateTime.of(2_022, Month.JULY, 14, 8, 2),
                        true
                ),
                Arguments.of(
                        LocalDateTime.of(2_022, Month.JUNE, 14, 0, 0),
                        LocalDateTime.of(2_022, Month.JUNE, 15, 0, 0),
                        false
                )
        );
    }

}