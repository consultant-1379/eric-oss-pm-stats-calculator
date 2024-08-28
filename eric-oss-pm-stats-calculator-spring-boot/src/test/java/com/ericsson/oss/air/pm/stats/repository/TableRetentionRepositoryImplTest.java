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

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity.RetentionPeriodTableEntityBuilder;
import com.ericsson.oss.air.pm.stats.test_utils.DatabasePropertiesMock;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.delegate.DatabaseDelegate;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@ExtendWith(MockitoExtension.class)
class TableRetentionRepositoryImplTest {
    @Container
    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:13.6")
            .withUsername("postgres")
            .withPassword("admin")
            .withDatabaseName("kpi_service_db");
    static final DatabaseDelegate DATABASE_DELEGATE = new JdbcDatabaseDelegate(POSTGRE_SQL_CONTAINER, "");

    Properties properties;

    TableRetentionRepositoryImpl objectUnderTest = new TableRetentionRepositoryImpl();

    @Nested
    class VerifyUncheckedSqlExceptions {
        @Test
        void shouldThrowUncheckedSqlException_onFindByCollectionId() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findByCollectionId(UUID.randomUUID()));
        }
    }

    @Nested
    class WhenDatabaseIsPresent {

        @BeforeEach
        void setUp() throws SQLException {
            properties = new Properties();
            properties.setProperty("user", POSTGRE_SQL_CONTAINER.getUsername());
            properties.setProperty("password", POSTGRE_SQL_CONTAINER.getPassword());

            ScriptUtils.runInitScript(DATABASE_DELEGATE, ("sql/initialize_retention_period_tables.sql"));
        }

        @AfterEach
        void tearDown() {
            ScriptUtils.runInitScript(DATABASE_DELEGATE, ("sql/tear_down_retention_period_tables.sql"));
        }

        @Test
        void shouldSaveAndFindRetentionPeriodByCollectionId() throws SQLException {
            final UUID collectionId = UUID.randomUUID();

            final RetentionPeriodTableEntity table60 = retentionPeriodTableEntity(null, collectionId, "table_60", 7);
            final RetentionPeriodTableEntity table1440 = retentionPeriodTableEntity(null, collectionId, "table_1440", 10);

            objectUnderTest.saveAll(getAdminConnection(), List.of(table60, table1440));

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final List<RetentionPeriodTableEntity> actual = objectUnderTest.findByCollectionId(collectionId);

                assertThat(actual).extracting(
                        RetentionPeriodTableEntity::getKpiCollectionId,
                        RetentionPeriodTableEntity::getTableName,
                        RetentionPeriodTableEntity::getRetentionPeriodInDays
                ).containsExactlyInAnyOrder(
                        Assertions.tuple(collectionId, "table_60", 7),
                        Assertions.tuple(collectionId, "table_1440", 10)
                );
            });
        }

        @Test
        void shouldUpdateOnConflict() throws SQLException {
            final UUID collectionId = UUID.randomUUID();

            final RetentionPeriodTableEntity table1 = retentionPeriodTableEntity(null, collectionId, "table_60", 7);
            final RetentionPeriodTableEntity table2 = retentionPeriodTableEntity(null, collectionId, "table_60", 8);

            objectUnderTest.saveAll(getAdminConnection(), List.of(table1));

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final List<RetentionPeriodTableEntity> actual = objectUnderTest.findByCollectionId(collectionId);

                assertThat(actual).extracting(
                        RetentionPeriodTableEntity::getKpiCollectionId,
                        RetentionPeriodTableEntity::getTableName,
                        RetentionPeriodTableEntity::getRetentionPeriodInDays
                ).containsExactlyInAnyOrder(Assertions.tuple(collectionId, "table_60", 7));
            });

            objectUnderTest.saveAll(getAdminConnection(), List.of(table2));

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final List<RetentionPeriodTableEntity> actual = objectUnderTest.findByCollectionId(collectionId);

                assertThat(actual).extracting(
                        RetentionPeriodTableEntity::getKpiCollectionId,
                        RetentionPeriodTableEntity::getTableName,
                        RetentionPeriodTableEntity::getRetentionPeriodInDays
                ).containsExactlyInAnyOrder(Assertions.tuple(collectionId, "table_60", 8));
            });
        }

        @Test
        void shouldDeleteTableRetention() throws SQLException {
            final UUID collectionId = UUID.randomUUID();

            final RetentionPeriodTableEntity table60 = retentionPeriodTableEntity(null, collectionId, "table_60", 7);
            final RetentionPeriodTableEntity table1440 = retentionPeriodTableEntity(null, collectionId, "table_1440", 10);

            objectUnderTest.saveAll(getAdminConnection(), List.of(table60, table1440));
            objectUnderTest.deleteRetentionForTables(getAdminConnection(), "table_60");

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final List<RetentionPeriodTableEntity> actual = objectUnderTest.findByCollectionId(collectionId);

                assertThat(actual).extracting(
                        RetentionPeriodTableEntity::getKpiCollectionId,
                        RetentionPeriodTableEntity::getTableName,
                        RetentionPeriodTableEntity::getRetentionPeriodInDays
                ).containsExactlyInAnyOrder(
                        Assertions.tuple(collectionId, "table_1440", 10)
                );
            });
        }
    }

    @SneakyThrows
    static Connection getAdminConnection() {
        return DriverManager.getConnection(POSTGRE_SQL_CONTAINER.getJdbcUrl(), POSTGRE_SQL_CONTAINER.getUsername(), POSTGRE_SQL_CONTAINER.getPassword());
    }

    static RetentionPeriodTableEntity retentionPeriodTableEntity(final Long id, final UUID collectionId, final String tableName, final int retentionPeriodInDays) {
        final RetentionPeriodTableEntityBuilder builder = RetentionPeriodTableEntity.builder();
        builder.withId(id);
        builder.withKpiCollectionId(collectionId);
        builder.withTableName(tableName);
        builder.withRetentionPeriodInDays(retentionPeriodInDays);
        return builder.build();
    }

}