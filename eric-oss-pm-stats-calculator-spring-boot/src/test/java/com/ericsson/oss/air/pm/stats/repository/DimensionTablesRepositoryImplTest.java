/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository;

import static com.ericsson.oss.air.pm.stats._util.TestHelpers.uuid;
import static com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers.database;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats._util.AssertionHelpers;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.repository._util.RepositoryHelpers;

import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

class DimensionTablesRepositoryImplTest {
    static UUID CALCULATION_ID = uuid("b10de8fb-2417-44cd-802b-19e0b13fd3a5");
    static UUID CALCULATION_ID_2 = uuid("d33fa2b3-f4ef-482f-af3c-186f054b2551");

    DimensionTablesRepositoryImpl objectUnderTest = new DimensionTablesRepositoryImpl();

    @Nested
    @DisplayName("Given an invalid JDBC URL")
    class GivenAnInvalidJdbcUrl {

        @Test
        void shouldThrowUncheckedSqlException_onFindTableNames() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findTableNamesForCalculation(CALCULATION_ID));
        }

        @Test
        void shouldThrowUncheckedSqlException_onFindAllTableNames() {
            AssertionHelpers.assertUncheckedSqlException(() -> objectUnderTest.findLostTableNames());
        }
    }

    @Nested
    @DisplayName("Given an available database")
    class GivenAnAvailableDatabase {

        EmbeddedDatabase embeddedDatabase;

        List<String> tables = List.of("tableName1", "tableName2");

        @BeforeEach
        void setUp() {
            embeddedDatabase = database(
                    "sql/initialize_dimension_tables.sql",
                    "sql/initialize_calculation.sql"
            );
        }

        @AfterEach
        void tearDown() {
            embeddedDatabase.shutdown();
        }

        @Test
        @SneakyThrows
        void shouldSave() {
            objectUnderTest.save(embeddedDatabase.getConnection(), tables, CALCULATION_ID);
            final List<Dimension> result = findAll();
            assertThat(result).containsExactlyInAnyOrder(
                    dimension("tableName1", CALCULATION_ID),
                    dimension("tableName2", CALCULATION_ID)
            );
        }

        @Test
        @SneakyThrows
        void shouldFindTableNames() {
            objectUnderTest.save(embeddedDatabase.getConnection(), tables, CALCULATION_ID);
            objectUnderTest.save(embeddedDatabase.getConnection(), List.of("should not find"), uuid("d33fa2b3-f4ef-482f-af3c-186f054b2551"));

            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final List<String> actual = objectUnderTest.findTableNamesForCalculation(CALCULATION_ID);
                assertThat(actual).containsExactlyInAnyOrder("tableName1", "tableName2");
            });
        }

        @Test
        @SneakyThrows
        void shouldFindLostTableNames() {
            objectUnderTest.save(embeddedDatabase.getConnection(), tables, CALCULATION_ID);
            objectUnderTest.save(embeddedDatabase.getConnection(), List.of("tableName3", "tableName4"), CALCULATION_ID_2);

            final Calculation calculation1 = calculation(CALCULATION_ID, KpiCalculationState.LOST);
            final Calculation calculation2 = calculation(CALCULATION_ID_2, KpiCalculationState.FINISHED);

            save(embeddedDatabase.getConnection(), calculation1);
            save(embeddedDatabase.getConnection(), calculation2);

            RepositoryHelpers.prepare(embeddedDatabase, () -> {
                final Set<String> actual = objectUnderTest.findLostTableNames();
                assertThat(actual).containsExactlyInAnyOrder("tableName1", "tableName2");
            });
        }

        private Calculation calculation(final UUID calculationId, final KpiCalculationState state) {
            return Calculation.builder().withCalculationId(calculationId).withKpiCalculationState(state).build();
        }

        List<Dimension> findAll() throws SQLException {
            final String sql = "SELECT * FROM kpi_service_db.kpi.dimension_tables";
            try (final Connection connection = embeddedDatabase.getConnection();
                 final Statement stmt = connection.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {
                final List<Dimension> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(dimension(rs.getString("table_name"), rs.getObject("calculation_id", UUID.class)));
                }
                return result;
            }
        }

        public void save(final Connection connection, final Calculation calculation) throws SQLException {
            final String sql = "INSERT INTO kpi_service_db.kpi.kpi_calculation(calculation_id, time_created, state, execution_group, kpi_type, collection_id) " +
                    "VALUES (?, now(), ?, 'group', 'type', ?)";

            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setObject(1, calculation.getCalculationId());
                preparedStatement.setString(2, calculation.getKpiCalculationState().name());
                preparedStatement.setObject(3, CollectionIdProxy.COLLECTION_ID);

                preparedStatement.executeUpdate();
            }
        }
    }

    @Data
    @Builder
    static class Dimension {
        private String tableName;
        private UUID calcId;
        private UUID collId;
    }

    static Dimension dimension(final String tableNAme, final UUID calcId) {
        return Dimension.builder()
                .tableName(tableNAme)
                .calcId(calcId)
                .collId(CollectionIdProxy.COLLECTION_ID)
                .build();
    }
}
