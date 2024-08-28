/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository._util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.sql.DataSource;

import com.ericsson.oss.air.pm.stats.embedded.AutoClosableEmbeddedDatabase;
import com.ericsson.oss.air.pm.stats.model.entity.CalculationReliability;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.test_utils.DatabasePropertiesMock;
import com.ericsson.oss.air.pm.stats.test_utils.DatabasePropertiesMock.TestRunner;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RepositoryHelpers {

    public static void prepare(final DataSource dataSource, final TestRunner testRunner) {
        DatabasePropertiesMock.prepare(databaseUrl(dataSource), properties(), testRunner);
    }

    public static Properties properties() {
        return MapUtils.toProperties(Map.of(
                "user", "SA"
        ));
    }

    @SneakyThrows
    public static String databaseUrl(final DataSource dataSource) {
        try (final Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        }
    }

    public static EmbeddedDatabase database(final String script, final String... scripts) {
        final EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder();

        embeddedDatabaseBuilder.setType(EmbeddedDatabaseType.H2);
        embeddedDatabaseBuilder.setName(String.format("%s;MODE=PostgreSQL", "kpi_service_db"));

        embeddedDatabaseBuilder.addScript(script); /* At least one script must be added */
        Arrays.stream(scripts).forEach(embeddedDatabaseBuilder::addScript);

        return AutoClosableEmbeddedDatabase.of(embeddedDatabaseBuilder.build());
    }

    public static void saveReadinessLog(final Connection connection, @NonNull final List<? extends ReadinessLog> readinessLogs) {
        readinessLogs.forEach(readinessLog -> saveReadinessLog(connection, readinessLog));
    }

    @SneakyThrows
    public static List<ComplexReadiness> findAllComplexReadiness(final Connection connection) {
        final String sql = "SELECT * FROM kpi_service_db.kpi.complex_readiness_log";

        try (final Statement stmt = connection.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {
            final List<ComplexReadiness> result = new ArrayList<>();

            while (rs.next()) {
                result.add(ComplexReadiness.of(
                        rs.getInt("simple_readiness_log_id"),
                        rs.getObject("complex_calculation_id", UUID.class))
                );
            }
            return result;
        }
    }

    @SneakyThrows
    public static List<CalculationReliability> findAllCalculationReliability(final Connection connection) {
        final String sql = "SELECT * FROM kpi_service_db.kpi.calculation_reliability";

        try (final Statement stmt = connection.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {
            final List<CalculationReliability> result = new ArrayList<>();

            while (rs.next()) {
                result.add(CalculationReliability.builder()
                        .withCalculationStartTime(rs.getTimestamp("calculation_start_time").toLocalDateTime())
                        .withReliabilityThreshold(rs.getTimestamp("reliability_threshold").toLocalDateTime())
                        .withKpiCalculationId(rs.getObject("calculation_id", UUID.class))
                        .withKpiDefinitionId(rs.getInt("kpi_definition_id"))
                        .build()
                );
            }
            return result;
        }
    }

    @SneakyThrows
    public static void saveReadinessLog(@NonNull final Connection connection, @NonNull final ReadinessLog readinessLog) {
        final String sql =
                "INSERT INTO kpi_service_db.kpi.readiness_log(id, datasource, collected_rows_count, earliest_collected_data, latest_collected_data, kpi_calculation_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, readinessLog.getId());
            preparedStatement.setString(2, readinessLog.getDatasource());
            preparedStatement.setLong(3, readinessLog.getCollectedRowsCount());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(readinessLog.getEarliestCollectedData()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(readinessLog.getLatestCollectedData()));
            preparedStatement.setObject(6, readinessLog.getKpiCalculationId());

            preparedStatement.executeUpdate();
        }
    }

    @Data(staticConstructor = "of")
    public static final class ComplexReadiness {
        private final Integer readinessId;
        private final UUID calculationId;
    }
}
