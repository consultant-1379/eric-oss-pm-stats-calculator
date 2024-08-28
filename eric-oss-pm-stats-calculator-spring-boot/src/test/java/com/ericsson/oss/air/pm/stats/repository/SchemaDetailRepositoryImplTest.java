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

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.schemaDetail;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.database;

import java.sql.SQLException;
import java.util.Optional;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

class SchemaDetailRepositoryImplTest {
    SchemaDetailRepositoryImpl objectUnderTest = new SchemaDetailRepositoryImpl();
    EmbeddedDatabase embeddedDatabase;

    @BeforeEach
    void setUp() {
        embeddedDatabase = database(
                "sql/initialize_schema_details.sql"
        );
    }

    @AfterEach
    void tearDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void shouldSave() throws SQLException {
        final SchemaDetail schemaDetail = schemaDetail(1, "namespace", "topic");

        final Integer actual = objectUnderTest.save(embeddedDatabase.getConnection(), schemaDetail);

        Assertions.assertThat(actual).isOne();
    }

    @Test
    void shouldFindById() throws SQLException {
        final SchemaDetail schemaDetail = schemaDetail(1,  "namespace", "topic");
        final Integer saved = objectUnderTest.save(embeddedDatabase.getConnection(), schemaDetail);

        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final Optional<SchemaDetail> actual = objectUnderTest.findById(saved);
            Assertions.assertThat(actual).hasValue(schemaDetail);
        });
    }

    @Test
    void shouldFindBy() throws SQLException {
        final SchemaDetail schemaDetail = schemaDetail(1,  "namespace", "topic");
        objectUnderTest.save(embeddedDatabase.getConnection(), schemaDetail);

        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final Optional<SchemaDetail> actual = objectUnderTest.findBy("topic", "namespace");
            Assertions.assertThat(actual).hasValue(schemaDetail);
        });
    }

    @Test
    void shouldNotFindById() {
        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final Optional<SchemaDetail> actual = objectUnderTest.findById(10);
            Assertions.assertThat(actual).isEmpty();
        });
    }

    @Test
    void shouldNotFindBy() {
        RepositoryHelpers.prepare(embeddedDatabase, () -> {
            final Optional<SchemaDetail> actual = objectUnderTest.findBy("topic", "namespace");
            Assertions.assertThat(actual).isEmpty();
        });
    }

    @Nested
    @DisplayName("Given an invalid JDBC URL")
    class GivenAnInvalidJdbcUrl {

        @Test
        void shouldThrowUncheckedSqlException_onFindById() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findById(1));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindBy() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findBy("topic", "namespace"));
        }
    }
}