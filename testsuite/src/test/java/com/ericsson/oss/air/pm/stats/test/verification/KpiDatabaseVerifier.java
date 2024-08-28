/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.verification;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static com.ericsson.oss.air.pm.stats.test.util.sql.SqlAssertions.assertCountQuery;
import static com.ericsson.oss.air.pm.stats.test.util.sql.SqlAssertions.assertTableContent;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationState;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.ReadinessLogResponse;
import com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.common.env.Environment;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.model.entity.Calculation;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.api.AbstractDataset;
import com.ericsson.oss.air.pm.stats.test.verification.dataset.aggregation.period.daily.simple.KpiCellSimpleDailyDateset;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.text.StringSubstitutor;
import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;

/**
 * Class used to encapsulate the verification of the contents of the KPI aggregation tables.
 */
@Slf4j
public final class KpiDatabaseVerifier {

    private static final Map<String, List<String>> SIMPLE_EXPECTED_TABLE_NAME_TO_COLUMNS = new HashMap<>(13);
    private static final int DEFAULT_RETENTION_PERIOD = 5;
    private static final String RETENTION_PERIOD = Environment.getEnvironmentValue("RETENTION_PERIOD_DAYS",
                                                                                   String.valueOf(DEFAULT_RETENTION_PERIOD));

    static {
        populateExpectedSimpleMaps(KpiCellSimpleDailyDateset.INSTANCE);
    }

    @SneakyThrows
    public static void verifyReadinessLogExistForExecutionGroup() {
        final String sql = "SELECT * FROM kpi_service_db.kpi.readiness_log";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(sql)) {

            final List<ReadinessLog> results = new ArrayList<>(9);
            while (resultSet.next()) {
                results.add(readinessLogMapper(resultSet));
            }

            results.forEach(readinessLog -> log.info("Collected '{}' rows for datasource '{}' between ['{}' - '{}']",
                                                     readinessLog.getCollectedRowsCount(),
                                                     readinessLog.getDatasource(),
                                                     readinessLog.getEarliestCollectedData(),
                                                     readinessLog.getLatestCollectedData()
            ));
            final LocalDateTime[] latestTimeStamp = new LocalDateTime[1];

            Assertions.assertThat(results).satisfiesExactlyInAnyOrder(
                    readinessLog -> {
                        Assertions.assertThat(readinessLog.getDatasource()).isEqualTo("dataSpace|category|fact_table");
                        Assertions.assertThat(readinessLog.getCollectedRowsCount()).isEqualTo(130L);
                    },
                    readinessLog -> {
                        Assertions.assertThat(readinessLog.getDatasource()).isEqualTo("5G|category|fact_table");
                        Assertions.assertThat(readinessLog.getCollectedRowsCount()).isEqualTo(128L);
                    },
                    readinessLog -> {
                        Assertions.assertThat(readinessLog.getDatasource()).isEqualTo("dataSpace|category|a_new_very_simple_kpi");
                        Assertions.assertThat(readinessLog.getCollectedRowsCount()).isEqualTo(42L);
                        latestTimeStamp[0] = readinessLog.getLatestCollectedData();
                    },
                    readinessLog -> {
                        Assertions.assertThat(readinessLog.getDatasource()).isEqualTo("dataSpace|category|simple_kpi_same_day");
                        Assertions.assertThat(readinessLog.getCollectedRowsCount()).isEqualTo(3L);
                    },
                    readinessLog -> {
                        Assertions.assertThat(readinessLog.getDatasource()).isEqualTo("dataSpace|category|a_new_very_simple_kpi");
                        Assertions.assertThat(readinessLog.getCollectedRowsCount()).isEqualTo(3L);
                        Assertions.assertThat(readinessLog.getEarliestCollectedData()).isAfter(latestTimeStamp[0].minusMinutes(181));
                    },
                    readinessLog -> {
                        Assertions.assertThat(readinessLog.getDatasource()).isEqualTo("dataSpace|category|limited_agg");
                        Assertions.assertThat(readinessLog.getCollectedRowsCount()).isEqualTo(18L);
                    },
                    readinessLog -> {
                        Assertions.assertThat(readinessLog.getDatasource()).isEqualTo("4G|PM_COUNTERS|SampleCellFDD_1");
                        Assertions.assertThat(readinessLog.getCollectedRowsCount()).isEqualTo(117L);
                    },
                    readinessLog -> {
                        Assertions.assertThat(readinessLog.getDatasource()).isEqualTo("4G|PM_COUNTERS|SampleRelation_1");
                        Assertions.assertThat(readinessLog.getCollectedRowsCount()).isEqualTo(78L);
                    }
            );
        }
    }

    @SneakyThrows
    public static void validateComplexReadinessLog(final String complexExecGroup, final int offset, final List<String> simpleExecutionGroups) {
        final List<CalculationReadinessLog> calculationReadinessLogs = fetchCalculationReadinessLog(complexExecGroup, offset);

        log.info(
                "Complex execution group '{}' depends on simple readiness logs:{}{}", complexExecGroup,
                System.lineSeparator(),
                calculationReadinessLogs.stream().map(Object::toString).collect(Collectors.joining(System.lineSeparator()))
        );

        assertThat(calculationReadinessLogs).as("Complex execution group '%s' must depend on '%d' readiness logs", complexExecGroup,
                                                simpleExecutionGroups.size()).hasSize(simpleExecutionGroups.size());

        final Calculation complexCalculation = complexCalculation(calculationReadinessLogs);
        final List<Calculation> simpleCalculations = simpleCalculations(calculationReadinessLogs);

        assertThat(simpleCalculations)
                .as("Complex execution group '%s' must depend on '%s' simple execution groups", complexExecGroup, simpleExecutionGroups)
                .extracting(Calculation::getExecutionGroup)
                .containsExactlyInAnyOrderElementsOf(simpleExecutionGroups);

        simpleCalculations.forEach(simpleCalculation -> {
            assertThat(simpleCalculation.getKpiCalculationState()).isIn(KpiCalculationState.getSuccessfulDoneKpiCalculationStates());
            assertThat(simpleCalculation.getTimeCompleted()).isBeforeOrEqualTo(complexCalculation.getTimeCompleted());
        });
    }

    @SneakyThrows
    public static void waitForReadinessLogExistForLateData() {
        final String sql = "SELECT 1 " +
                           "FROM kpi_service_db.kpi.readiness_log " +
                           "WHERE collected_rows_count = 3 " +
                           "AND datasource = 'dataSpace|category|a_new_very_simple_kpi'";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            final StopWatch stopWatch = StopWatch.createStarted();
            do {
                log.info("Waiting for readiness log for late data... Elapsed: {}", stopWatch);
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return;
                    }
                }
                TimeUnit.SECONDS.sleep(5);
            } while (true);
        }
    }

    public static void verifyCalculationExistForExecutionGroup(final String executionGroup, KpiCalculationState... states) {
        verifyCalculationExistForExecutionGroup(executionGroup, 0, states);
    }

    @SneakyThrows
    public static void verifyCalculationExistForExecutionGroup(final String executionGroup, Integer offset, KpiCalculationState... states) {
        final String sql = "SELECT 1 " +
                           "FROM kpi_service_db.kpi.kpi_calculation " +
                           "WHERE state = ANY(?) " +
                           "AND execution_group = ? " +
                           "OFFSET ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setArray(1, connection.createArrayOf("VARCHAR", states));
            preparedStatement.setString(2, executionGroup);
            preparedStatement.setInt(3, offset);

            final StopWatch stopWatch = StopWatch.createStarted();
            do {
                log.info("Waiting for calculation in state {} for {}... Elapsed: {}", states, executionGroup, stopWatch);
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return;
                    }
                }
                TimeUnit.SECONDS.sleep(5);
            } while (true);
        }
    }

    @SneakyThrows
    public static void verifyCalculationDoesNotExistsForExecutionGroup(final String executionGroup) {
        final String sql = "SELECT 1 FROM kpi_service_db.kpi.kpi_calculation WHERE execution_group = ?";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, executionGroup);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                Assertions.assertThat(resultSet.next()).isFalse();
            }
        }
    }

    public static void assertTableContents(final AbstractDataset abstractDataset) {
        log.info("Asserting table: {}", abstractDataset.getTableName());
        assertTableContent(abstractDataset.getTableName(), abstractDataset.getExpectedColumns(), abstractDataset.getDataset());
    }

    public static void verifyKpiDefinitionTable(final int kpiDefinitionsCount) {
        assertCountQuery(kpiDefinitionsCount, "kpi_definition");
    }

    public static void verifyCalculationReliabilityTable(final int calculationReliabilityCount) {
        assertCountQuery(calculationReliabilityCount, "calculation_reliability");
    }

    public static void verifySimpleKpiOutputTableColumns() {
        for (final String tableName : SIMPLE_EXPECTED_TABLE_NAME_TO_COLUMNS.keySet()) {
            Assertions.assertThat(verifyTableColumns(tableName, SIMPLE_EXPECTED_TABLE_NAME_TO_COLUMNS.get(tableName))).isTrue();
        }
    }

    public static boolean verifyTableColumns(final String tableName, final List<String> expectedColumns) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM %s LIMIT 1", tableName))) {
            for (final String column : expectedColumns) {
                resultSet.findColumn(column);
            }
            return true;
        } catch (final SQLException e) {
            log.error("Unable to verify table columns.", e);
            return false;
        }
    }

    @SneakyThrows
    public static void verifyLatestProcessedOffsetsTable(final String topic, final String executionGroup, final long expectedSum) {
        final String sql =
                "SELECT offsets.topic_partition, offsets.topic_partition_offset " +
                "FROM kpi_service_db.kpi.latest_processed_offsets offsets " +
                "INNER JOIN kpi_service_db.kpi.kpi_execution_groups exe " +
                "ON exe.id = offsets.execution_group_id " +
                "WHERE topic_name = ? AND execution_group = ? " +
                "ORDER BY topic_partition_offset";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, topic);
            preparedStatement.setString(2, executionGroup);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                long offsetSum = 0;

                while (resultSet.next()) {
                    final int partition = resultSet.getInt(1);
                    final long offset = resultSet.getLong(2);

                    log.info("For topic partition '{}' found '{}' offsets", new TopicPartition(topic, partition), offset);

                    offsetSum += offset;
                }

                assertThat(offsetSum)
                          .as(String.format("Offset sum failed for %s execution group for %s topic", executionGroup, topic))
                          .isEqualTo(expectedSum);
            }
        }
    }

    public static void validateTableDefinition(final String tableName, final List<String> expectedPrimaryKeys,
                                               final Map<String, KpiDataType> expectedColumnTypes) {
        try (final Connection connection = DriverManager.getConnection(DatabaseProperties.getKpiServiceJdbcConnection(),
                                                                       DatabaseProperties.getKpiServiceJdbcProperties())) {
            final Set<String> primaryKeys = getPrimaryKeysFromTable(connection, tableName);
            assertThat(primaryKeys).size().isEqualTo(expectedPrimaryKeys.size());
            assertThat(primaryKeys).containsAll(expectedPrimaryKeys);

            final Map<String, KpiDataType> dbColumns = getColumnsFromTable(connection, tableName);
            assertThat(dbColumns).size().isEqualTo(expectedColumnTypes.size());
            assertThat(dbColumns).containsAllEntriesOf(expectedColumnTypes);
        } catch (final SQLException sqlException) {
            log.warn("Error opening connection to table: '{}'", tableName, sqlException);
        }
    }

    public static void validatePartitionedTableDefinition(final String tableName, final List<Column> expectedUniqueKeys,
                                                          final Map<String, KpiDataType> expectedColumnTypes) {
        try (final Connection connection = DriverManager.getConnection(DatabaseProperties.getKpiServiceJdbcConnection(),
                                                                       DatabaseProperties.getKpiServiceJdbcProperties())) {
            final List<PartitionUniqueIndex> uniqueIndexesForTable = getUniqueIndexForTablePartitions(connection,
                                                                                                      tableName);

            assertThat(uniqueIndexesForTable).hasSize(Integer.parseInt(RETENTION_PERIOD) + 3);

            final List<PartitionUniqueIndex> expectedUniqueIndexesForTable = getExpectedUniqueIndexesForTable(tableName, expectedUniqueKeys);
            assertThat(uniqueIndexesForTable).usingRecursiveFieldByFieldElementComparator()
                                             .containsExactlyInAnyOrderElementsOf(expectedUniqueIndexesForTable);

            final Map<String, KpiDataType> dbColumns = getColumnsFromTable(connection, tableName);
            assertThat(dbColumns).size().isEqualTo(expectedColumnTypes.size());
            assertThat(dbColumns).containsAllEntriesOf(expectedColumnTypes);
        } catch (final SQLException sqlException) {
            log.warn("Error opening connection to table: '{}'", tableName, sqlException);
        }
    }

    //  TODO: Temporarily migrated to be able to delete form production code
    //        Refactor these in an other commit
    public static Set<String> getPrimaryKeysFromTable(final Connection connection, final String tableName) {
        final Set<String> dbPrimaryKeys = new HashSet<>();
        try (final ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(null, null, tableName)) {
            while (primaryKeys.next()) {
                final String columnName = primaryKeys.getString("COLUMN_NAME");
                dbPrimaryKeys.add(columnName);
            }
        } catch (final SQLException e) { //NOSONAR Exception is suitably logged
            log.warn("Failed to read primary keys for table '{}' - {}", tableName, e.getClass());
        }
        return Collections.unmodifiableSet(dbPrimaryKeys);
    }

    public static Map<String, KpiDataType> getColumnsFromTable(final Connection connection, final String tableName) {
        final Map<String, KpiDataType> dbColumns = new HashMap<>();
        try (final ResultSet columns = connection.getMetaData().getColumns(null, null, tableName, null)) {
            while (columns.next()) {
                final String columnName = columns.getString("COLUMN_NAME");
                final String typeName = columns.getString("TYPE_NAME");
                final Optional<KpiDataType> kpiDataTypeOptional = getKpiDataType(typeName);
                if (kpiDataTypeOptional.isPresent()) {
                    dbColumns.put(columnName, kpiDataTypeOptional.get());
                } else {
                    log.warn("Failed to find mapping for column '{}' - '{}'", columnName, typeName);
                }
            }
        } catch (final SQLException e) {
            log.warn("Failed to read columns for table '{}' - {}", tableName, e.getClass());
        }
        return Collections.unmodifiableMap(dbColumns);
    }

    static Calculation calculationMapper(@NonNull final ResultSet resultSet) throws SQLException {
        return Calculation.builder()
                          .withCalculationId(resultSet.getObject("calculation_id", UUID.class))
                          .withTimeCreated(resultSet.getTimestamp("time_created").toLocalDateTime())
                          .withTimeCompleted(resultSet.getTimestamp("time_completed").toLocalDateTime())
                          .withKpiCalculationState(KpiCalculationState.valueOf(resultSet.getString("state")))
                          .withParameters(resultSet.getString("parameters"))
                          .withExecutionGroup(resultSet.getString("execution_group"))
                          .withKpiType(KpiType.valueOf(resultSet.getString("kpi_type")))
                          .build();
    }

    static ReadinessLog readinessLogMapper(@NonNull final ResultSet resultSet) throws SQLException {
        return ReadinessLog.builder()
                           .withDatasource(resultSet.getString("datasource"))
                           .withCollectedRowsCount(resultSet.getLong("collected_rows_count"))
                           .withEarliestCollectedData(resultSet.getTimestamp("earliest_collected_data").toLocalDateTime())
                           .withLatestCollectedData(resultSet.getTimestamp("latest_collected_data").toLocalDateTime())
                           .withKpiCalculationId(resultSet.getObject("kpi_calculation_id", UUID.class))
                           .build();
    }

    static ReadinessLogResponse readinessLogResponseMapper(@NonNull final ResultSet resultSet) throws SQLException {
        return ReadinessLogResponse.builder()
                                   .withCollectedRowsCount(resultSet.getLong("collected_rows_count"))
                                   .withEarliestCollectedData(resultSet.getTimestamp("earliest_collected_data").toLocalDateTime())
                                   .withLatestCollectedData(resultSet.getTimestamp("latest_collected_data").toLocalDateTime())
                                   .withDatasource(resultSet.getString("datasource"))
                                   .build();
    }

    @SneakyThrows
    public static List<ReadinessLogResponse> fetchReadinessLogResponseByCalculationId(final UUID calculationId) {
        final String sql = "SELECT * " +
                           "FROM kpi_service_db.kpi.readiness_log " +
                           "WHERE kpi_calculation_id = ? ";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            final List<ReadinessLogResponse> readinessLogResponses = new ArrayList<>();

            preparedStatement.setObject(1, calculationId);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    readinessLogResponses.add(readinessLogResponseMapper(resultSet));
                }
                return readinessLogResponses;
            }
        }
    }

    public static Calculation forceFetchCalculationByExecutionGroup(final String executionGroup) {
        return fetchCalculationByExecutionGroup(executionGroup).orElseThrow(() -> new UncheckedSqlException(
                String.format("Calculation with id '%s' is not found", executionGroup)
        ));
    }

    @SneakyThrows
    private static Optional<Calculation> fetchCalculationByExecutionGroup(final String executionGroup) {
        final String sql = "SELECT * " +
                           "FROM kpi_service_db.kpi.kpi_calculation " +
                           "WHERE execution_group = ? ";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, executionGroup);
            final ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next()
                    ? Optional.of(calculationMapper(resultSet))
                    : Optional.empty();
        }
    }

    public static List<CalculationReadinessLog> fetchCalculationReadinessLog(final String executionGroup, final int offset) throws SQLException {
        final String sql =
                "SELECT *" +
                "FROM kpi_service_db.kpi.readiness_log AS readiness_log " +
                "INNER JOIN kpi_service_db.kpi.complex_readiness_log AS complex_readiness_log " +
                "        ON complex_readiness_log.simple_readiness_log_id = readiness_log.id " +
                "INNER JOIN kpi_service_db.kpi.kpi_calculation AS calculation " +
                "        ON complex_readiness_log.complex_calculation_id = calculation.calculation_id " +
                "WHERE complex_readiness_log.complex_calculation_id = " +
                "(SELECT kpi_service_db.kpi.kpi_calculation.calculation_id " +
                "FROM kpi_calculation WHERE execution_group = ? " +
                "ORDER BY time_completed " +
                "OFFSET ? " +
                "LIMIT 1)";

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            final List<CalculationReadinessLog> results = new ArrayList<>();

            preparedStatement.setString(1, executionGroup);
            preparedStatement.setInt(2, offset);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(CalculationReadinessLog.of(calculationMapper(resultSet), readinessLogMapper(resultSet)));
                }

                return results;
            }
        }
    }

    private static Calculation forceFindCalculationById(final UUID calculationId) throws SQLException {
        return findCalculationById(calculationId).orElseThrow(() -> new UncheckedSqlException(
                String.format("Calculation with id '%s' is not found", calculationId)
        ));
    }

    private static Optional<Calculation> findCalculationById(final UUID calculationId) throws SQLException {
        final String sql =
                "SELECT * " +
                "FROM kpi_service_db.kpi.kpi_calculation " +
                "WHERE calculation_id = ?";
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, calculationId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(calculationMapper(resultSet))
                        : Optional.empty();
            }
        }
    }

    private static List<UUID> simpleCalculationIds(@NonNull final List<CalculationReadinessLog> calculationReadinessLogs) {
        return calculationReadinessLogs.stream().map(CalculationReadinessLog::getKpiCalculationId).collect(Collectors.toList());
    }

    public static Calculation complexCalculation(@NonNull final List<CalculationReadinessLog> calculationReadinessLogs) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(calculationReadinessLogs), "calculationReadinessLogs is empty");
        return calculationReadinessLogs.iterator().next().getCalculation();
    }

    public static List<ReadinessLog> complexReadinessLog(@NonNull final List<CalculationReadinessLog> calculationReadinessLogs) {
        return calculationReadinessLogs.stream().map(CalculationReadinessLog::getReadinessLog).collect(Collectors.toList());
    }

    private static List<Calculation> simpleCalculations(@NonNull final List<CalculationReadinessLog> calculationReadinessLogs) throws SQLException {
        final List<Calculation> result = new ArrayList<>(calculationReadinessLogs.size());
        for (final UUID simpleCalculationId : simpleCalculationIds(calculationReadinessLogs)) {
            result.add(forceFindCalculationById(simpleCalculationId));
        }
        return result;
    }

    private static Optional<KpiDataType> getKpiDataType(final String postgresType) {
        return KpiDataType.valuesAsList()
                          .stream()
                          .filter(dataType -> dataType.getColumnType().equalsIgnoreCase(postgresType))
                          .findFirst();
    }

    public static List<PartitionUniqueIndex> getUniqueIndexForTablePartitions(final Connection connection, final String tableName) throws SQLException {
        // TODO: Use prepared statement instead
        String selectUniqueIndexSql = "SELECT * FROM pg_indexes WHERE tablename like '${table}_p_%';";

        final Map<String, String> alterTableColumnValues = new HashMap<>(3);
        alterTableColumnValues.put("table", tableName);
        selectUniqueIndexSql = StringSubstitutor.replace(selectUniqueIndexSql, alterTableColumnValues);
        final List<PartitionUniqueIndex> partitionUniqueIndexes = new ArrayList<>();
        try (final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectUniqueIndexSql)) {

            while (resultSet.next()) {
                final PartitionUniqueIndex partitionUniqueIndex = new PartitionUniqueIndex(
                        resultSet.getObject("tablename").toString(),
                        resultSet.getObject("indexname").toString(),
                        getUniqueIndexColumnsFromIndexDef(resultSet.getObject("indexdef").toString()));
                partitionUniqueIndexes.add(partitionUniqueIndex);
            }
            return partitionUniqueIndexes;
        }
    }

    private static List<Column> getUniqueIndexColumnsFromIndexDef(final String indexdef) {
        final List<String> indexColumns = Arrays.asList(indexdef.substring(indexdef.lastIndexOf('(') + 1, indexdef.lastIndexOf(')')).split(","));
        return indexColumns.stream().map(String::trim).map(Column::of).sorted(Comparator.comparing(Column::getName)).collect(Collectors.toList());
    }

    private static List<PartitionUniqueIndex> getExpectedUniqueIndexesForTable(final String tableName, final List<Column> expectedUniqueKeys) {
        final List<PartitionUniqueIndex> expectedUniqueIndexesForTable = new ArrayList<>();
        IntStream.range(-2, Integer.parseInt(RETENTION_PERIOD) + 1).forEach(value -> {
            final PartitionUniqueIndex partitionUniqueIndex = new PartitionUniqueIndex(
                    tableName + "_p_" + LocalDate.now().minusDays(value).format(DateTimeFormatter.ofPattern("yyyy_MM_dd")),
                    tableName + "_p_" + LocalDate.now().minusDays(value).format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + "_ui",
                    expectedUniqueKeys
            );
            expectedUniqueIndexesForTable.add(partitionUniqueIndex);
        });

        return expectedUniqueIndexesForTable;
    }

    private static void populateExpectedSimpleMaps(final AbstractDataset expectedDataset) {
        SIMPLE_EXPECTED_TABLE_NAME_TO_COLUMNS.put(expectedDataset.getTableName(), expectedDataset.getExpectedColumns());
    }

    @Data(staticConstructor = "of")
    public static final class CalculationReadinessLog {
        private final Calculation calculation;
        private final ReadinessLog readinessLog;

        public UUID getKpiCalculationId() {
            return readinessLog.getKpiCalculationId();
        }
    }
}
