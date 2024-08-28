/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.container;

import static org.mockito.Mockito.spy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.PartitionRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;
import com.ericsson.oss.air.pm.stats.repository.util.partition.PartitionUniqueIndex;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlTableModifierImpl;
import com.ericsson.oss.air.pm.stats.test_utils.DatabasePropertiesMock;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.delegate.DatabaseDelegate;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class PartitionRepositoryImplTest {
    @Container
    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:13.6");

    private static final DatabaseDelegate DATABASE_DELEGATE = new JdbcDatabaseDelegate(POSTGRE_SQL_CONTAINER, "");

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd");

    static final LocalDate TEST_LOCAL_DATE = LocalDate.of(2_022, Month.MAY, 26);
    static final String AGGREGATION_BEGIN_TIME = "aggregation_begin_time";
    static final String AGG_COLUMN_0 = "agg_column_0";

    static final String KPI_ROLLING_AGGREGATION_1440 = "kpi_rolling_aggregation_1440";
    static final Table TABLE_KPI_ROLLING_AGGREGATION_1440 = Table.of(KPI_ROLLING_AGGREGATION_1440);

    static final String PARTITION_NAME_1 = partitionName(TEST_LOCAL_DATE);
    static final String PARTITION_NAME_2 = partitionName(TEST_LOCAL_DATE.plusDays(1));

    List<String> defaultPartitionNames;

    @InjectMocks
    SqlTableModifierImpl sqlTableModifierSpy = spy(new SqlTableModifierImpl());

    @InjectMocks
    PartitionRepositoryImpl objectUnderTest;

    @SneakyThrows
    static Connection getAdminConnection() {
        return DriverManager.getConnection(POSTGRE_SQL_CONTAINER.getJdbcUrl(), POSTGRE_SQL_CONTAINER.getUsername(), POSTGRE_SQL_CONTAINER.getPassword());
    }

    @BeforeEach
    @SneakyThrows
    void setUp() {
        defaultPartitionNames = Arrays.asList("kpi_rolling_aggregation_1440_p_2022_05_26", "kpi_rolling_aggregation_1440_p_2022_05_27");

        ScriptUtils.runInitScript(DATABASE_DELEGATE, "sql/container/partition/initialize_partition.sql");
    }

    @AfterEach
    void tearDown() {
        ScriptUtils.runInitScript(DATABASE_DELEGATE, "sql/container/partition/drop_partition.sql");
    }

    @Nested
    @DisplayName("when something goes wrong")
    class WhenSomethingGoesWrong {

        @Test
        void shouldThrowUncheckedSqlException_GetPartitionNamesForTable() {
            DatabasePropertiesMock.prepare("invalidUrl", new Properties(), () -> {
                Assertions.assertThatThrownBy(() -> objectUnderTest.getPartitionNamesForTable(KPI_ROLLING_AGGREGATION_1440))
                        .hasRootCauseInstanceOf(SQLException.class)
                        .isInstanceOf(UncheckedSqlException.class);
            });
        }
    }

    @Nested
    @DisplayName("when Connection is internal detail")
    class WhenConnectionIsInternalDetail {

        Properties properties;

        @BeforeEach
        void setUp() {
            properties = new Properties();
            properties.setProperty("user", POSTGRE_SQL_CONTAINER.getUsername());
            properties.setProperty("password", POSTGRE_SQL_CONTAINER.getPassword());
        }

        @Test
        void shouldGetPartitionNamesForTable() {
            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final List<String> actual = objectUnderTest.getPartitionNamesForTable(KPI_ROLLING_AGGREGATION_1440);

                Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(defaultPartitionNames);
            });
        }

        @Test
        void shouldDropPartitions() {
            SqlExecutor.execute(connection -> {
                DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                    final List<String> actual = objectUnderTest.getPartitionNamesForTable(KPI_ROLLING_AGGREGATION_1440);

                    Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(defaultPartitionNames);
                });

                Assertions.assertThatNoException()
                        .isThrownBy(() -> objectUnderTest.dropPartitions(connection, defaultPartitionNames));

                DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                    final List<String> actual = objectUnderTest.getPartitionNamesForTable(KPI_ROLLING_AGGREGATION_1440);

                    Assertions.assertThat(actual).isEmpty();
                });
            });
        }

        @Test
        void shouldCreatePartitions() {
            SqlExecutor.execute(connection -> {
                Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.createPartitions(connection, partitionsToCreate()));
            });

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final List<String> actual = objectUnderTest.getPartitionNamesForTable(KPI_ROLLING_AGGREGATION_1440);

                final List<String> expected = new ArrayList<>(defaultPartitionNames);
                expected.add("kpi_rolling_aggregation_1440_p_2022_05_28");
                expected.add("kpi_rolling_aggregation_1440_p_2022_05_29");

                Assertions.assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
            });
        }

        @Test
        void shouldGetUniqueIndexForTablePartitions() {
            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final List<PartitionUniqueIndex> actual = objectUnderTest.findAllPartitionUniqueIndexes(TABLE_KPI_ROLLING_AGGREGATION_1440);

                Assertions.assertThat(actual).satisfiesExactlyInAnyOrder(partition1 -> {
                    Assertions.assertThat(partition1.getPartitionName()).isEqualTo(PARTITION_NAME_1);
                    Assertions.assertThat(partition1.getUniqueIndexName()).isEqualTo("%s_ui", PARTITION_NAME_1);
                    Assertions.assertThat(partition1.indexColumns()).containsExactly(
                            Column.of(AGG_COLUMN_0),
                            Column.of(AGGREGATION_BEGIN_TIME)
                    );
                }, partition2 -> {
                    Assertions.assertThat(partition2.getPartitionName()).isEqualTo(PARTITION_NAME_2);
                    Assertions.assertThat(partition2.getUniqueIndexName()).isEqualTo("%s_ui", PARTITION_NAME_2);
                    Assertions.assertThat(partition2.indexColumns()).containsExactly(
                            Column.of(AGG_COLUMN_0),
                            Column.of(AGGREGATION_BEGIN_TIME)
                    );
                });
            });
        }

        @Test
        void shouldDropUniqueIndex() {
            final List<PartitionUniqueIndex> uniqueIndices = readUniqueIndexes(TABLE_KPI_ROLLING_AGGREGATION_1440);
            ;

            uniqueIndices.forEach(uniqueIndex ->
                    SqlExecutor.execute(connection -> {
                        objectUnderTest.dropUniqueIndex(connection, uniqueIndex);
                    })
            );

            final List<PartitionUniqueIndex> actual = readUniqueIndexes(TABLE_KPI_ROLLING_AGGREGATION_1440);
            Assertions.assertThat(actual).isEmpty();
        }

        @Test
        void shouldCreateUniqueIndex() {
            SqlExecutor.execute(connection -> {
                final PartitionUniqueIndex uniqueIndex = new PartitionUniqueIndex(PARTITION_NAME_1,
                        "unique_index",
                        Collections.singletonList(Column.of(AGGREGATION_BEGIN_TIME)));
                objectUnderTest.createUniqueIndex(connection, uniqueIndex);
            });

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                final List<PartitionUniqueIndex> actual = objectUnderTest.findAllPartitionUniqueIndexes(TABLE_KPI_ROLLING_AGGREGATION_1440);

                Assertions.assertThat(actual).satisfiesExactlyInAnyOrder(partition1 -> {
                    Assertions.assertThat(partition1.getPartitionName()).isEqualTo(PARTITION_NAME_1);
                    Assertions.assertThat(partition1.getUniqueIndexName()).isEqualTo("%s_ui", PARTITION_NAME_1);
                    Assertions.assertThat(partition1.indexColumns()).containsExactly(
                            Column.of(AGG_COLUMN_0),
                            Column.of(AGGREGATION_BEGIN_TIME));
                }, partition2 -> {
                    Assertions.assertThat(partition2.getPartitionName()).isEqualTo(PARTITION_NAME_2);
                    Assertions.assertThat(partition2.getUniqueIndexName()).isEqualTo("%s_ui", PARTITION_NAME_2);
                    Assertions.assertThat(partition2.indexColumns()).containsExactly(
                            Column.of(AGG_COLUMN_0),
                            Column.of(AGGREGATION_BEGIN_TIME));
                }, partition3 -> {
                    Assertions.assertThat(partition3.getPartitionName()).isEqualTo(PARTITION_NAME_1);
                    Assertions.assertThat(partition3.getUniqueIndexName()).isEqualTo("unique_index");
                    Assertions.assertThat(partition3.indexColumns()).containsExactly(
                            Column.of(AGGREGATION_BEGIN_TIME));
                });
            });
        }

        private List<PartitionUniqueIndex> readUniqueIndexes(final Table table) {
            final List<PartitionUniqueIndex> result = new ArrayList<>();

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                result.addAll(objectUnderTest.findAllPartitionUniqueIndexes(table));
            });

            return result;
        }
    }

    static List<Partition> partitionsToCreate() {
        return Arrays.asList(new Partition(partitionName(TEST_LOCAL_DATE.plusDays(2)),
                        KPI_ROLLING_AGGREGATION_1440,
                        TEST_LOCAL_DATE.plusDays(2),
                        TEST_LOCAL_DATE.plusDays(3),
                        Collections.singleton(AGGREGATION_BEGIN_TIME)),
                new Partition(partitionName(TEST_LOCAL_DATE.plusDays(3)),
                        KPI_ROLLING_AGGREGATION_1440,
                        TEST_LOCAL_DATE.plusDays(3),
                        TEST_LOCAL_DATE.plusDays(4),
                        Collections.singleton(AGGREGATION_BEGIN_TIME)));
    }

    private static final class SqlExecutor {
        @SneakyThrows
        static void execute(final SqlRunner sqlRunner) {
            try (final Connection connection = POSTGRE_SQL_CONTAINER.createConnection(StringUtils.EMPTY)) {
                sqlRunner.run(connection);
            }
        }

        @FunctionalInterface
        interface SqlRunner {
            void run(Connection connection) throws SQLException;
        }
    }

    private static String partitionName(final LocalDate localDate) {
        return String.format("%s_p_%s", KPI_ROLLING_AGGREGATION_1440, localDate.format(FORMATTER));
    }
}