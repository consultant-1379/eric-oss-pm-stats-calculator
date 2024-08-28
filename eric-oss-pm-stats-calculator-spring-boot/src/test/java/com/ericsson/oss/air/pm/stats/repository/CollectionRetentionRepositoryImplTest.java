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
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity.RetentionPeriodCollectionEntityBuilder;
import com.ericsson.oss.air.pm.stats.repository.api.CollectionRetentionRepository;
import com.ericsson.oss.air.pm.stats.test_utils.DatabasePropertiesMock;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.delegate.DatabaseDelegate;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class CollectionRetentionRepositoryImplTest {
    @Container
    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:13.6")
            .withUsername("postgres")
            .withPassword("admin")
            .withDatabaseName("kpi_service_db");
    static final DatabaseDelegate DATABASE_DELEGATE = new JdbcDatabaseDelegate(POSTGRE_SQL_CONTAINER, "");

    Properties properties;
    CollectionRetentionRepository objectUnderTest = new CollectionRetentionRepositoryImpl();

    @Nested
    class VerifyUncheckedSqlExceptions {
        @Test
        void shouldThrowUncheckedSqlExceptionWhenFind() {
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
        void shouldSaveAndFindRetentionPeriodByCalculationId() throws SQLException {
            final UUID collectionId = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");

            final long id = objectUnderTest.save(getAdminConnection(), retentionPeriodCollectionEntity(null, collectionId, 7));

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final Optional<RetentionPeriodCollectionEntity> actual = objectUnderTest.findByCollectionId(collectionId);
                assertThat(actual).hasValue(retentionPeriodCollectionEntity(id, collectionId, 7));
            });
        }

        @Test
        void shouldUpsertAndFindRetentionPeriodByCalculationId() throws SQLException {
            final UUID collectionId = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");

            final long id1 = objectUnderTest.save(getAdminConnection(), retentionPeriodCollectionEntity(null, collectionId, 7));

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final Optional<RetentionPeriodCollectionEntity> actual = objectUnderTest.findByCollectionId(collectionId);
                assertThat(actual).hasValue(retentionPeriodCollectionEntity(id1, collectionId, 7));
            });

            final long id2 = objectUnderTest.save(getAdminConnection(), retentionPeriodCollectionEntity(null, collectionId, 10));

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final Optional<RetentionPeriodCollectionEntity> actual = objectUnderTest.findByCollectionId(collectionId);
                assertThat(actual).hasValue(retentionPeriodCollectionEntity(id2, collectionId, 10));
            });
        }
    }

    @SneakyThrows
    static Connection getAdminConnection() {
        return DriverManager.getConnection(POSTGRE_SQL_CONTAINER.getJdbcUrl(), POSTGRE_SQL_CONTAINER.getUsername(), POSTGRE_SQL_CONTAINER.getPassword());
    }

    static RetentionPeriodCollectionEntity retentionPeriodCollectionEntity(final Long id, final UUID collectionId, final int retentionPeriodInDays) {
        final RetentionPeriodCollectionEntityBuilder builder = RetentionPeriodCollectionEntity.builder();
        builder.withId(id);
        builder.withKpiCollectionId(collectionId);
        builder.withRetentionPeriodInDays(retentionPeriodInDays);
        return builder.build();
    }
}
