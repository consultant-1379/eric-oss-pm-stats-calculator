/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.container;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.db.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.exception.EntityNotFoundException;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.repository.DatabaseRepositoryImpl;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlAppenderImpl;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlTableGeneratorImpl;
import com.ericsson.oss.air.pm.stats.repository.util.table.SqlTableModifierImpl;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.ColumnDefinition;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;
import com.ericsson.oss.air.pm.stats.test_utils.DatabasePropertiesMock;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.assertj.db.type.Request;
import org.assertj.db.type.Source;
import org.assertj.db.type.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.delegate.DatabaseDelegate;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith(MockitoExtension.class)
class DatabaseRepositoryImplTest {
    static final String ADMIN_USER = "postgres";
    static final String ADMIN_PASSWORD = "admin";

    static final String KPI_SERVICE_DB = "kpi_service_db";

    @Container
    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:13.6")
            .withUsername(ADMIN_USER)
            .withPassword(ADMIN_PASSWORD)
            .withDatabaseName(KPI_SERVICE_DB);
    private static final DatabaseDelegate DATABASE_DELEGATE = new JdbcDatabaseDelegate(POSTGRE_SQL_CONTAINER, "");

    static final String PRIMARY_KEY_TABLE = "primary_key_table";
    static final String KPI_ROLLING_AGGREGATION_1440 = "kpi_rolling_aggregation_1440";

    static final com.ericsson.oss.air.pm.stats.common.model.collection.Table TABLE_PRIMARY_KEY = table(PRIMARY_KEY_TABLE);
    static final com.ericsson.oss.air.pm.stats.common.model.collection.Table TABLE_KPI_ROLLING_AGGREGATION_1440 = table(KPI_ROLLING_AGGREGATION_1440);

    static Source adminSource;

    Properties properties;

    @InjectMocks
    SqlAppenderImpl sqlAppenderSpy = spy(new SqlAppenderImpl());
    @InjectMocks
    SqlTableGeneratorImpl sqlTableGeneratorSpy = spy(new SqlTableGeneratorImpl());
    @InjectMocks
    SqlTableModifierImpl sqlTableModifierSpy = spy(new SqlTableModifierImpl());

    @InjectMocks
    DatabaseRepositoryImpl objectUnderTest;

    @BeforeAll
    static void beforeAll() {
        adminSource = new Source(POSTGRE_SQL_CONTAINER.getJdbcUrl(), ADMIN_USER, ADMIN_PASSWORD);
    }

    @BeforeEach
    void setUp() {
        properties = new Properties();
        properties.setProperty("user", POSTGRE_SQL_CONTAINER.getUsername());
        properties.setProperty("password", POSTGRE_SQL_CONTAINER.getPassword());

        ScriptUtils.runInitScript(DATABASE_DELEGATE, "sql/container/initialize_container.sql");
    }

    @AfterEach
    void tearDown() {
        ScriptUtils.runInitScript(DATABASE_DELEGATE, "sql/container/drop_container.sql");
    }

    @Test
    void shouldDetectPartitionedTables() {
        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final boolean actual = objectUnderTest.doesTableExistByName("kpi_rolling_aggregation_1440");
            Assertions.assertThat(actual).isTrue();
        });
    }

    @Test
    void shouldFindAllPrimaryKeys() {
        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final List<Column> actual = objectUnderTest.findAllPrimaryKeys(table(PRIMARY_KEY_TABLE));
            Assertions.assertThat(actual).containsExactlyInAnyOrder(Column.of("column1"), Column.of("column3"));
        });
    }

    @Test
    void shouldDropPrimaryKeyConstraint() throws SQLException {
        objectUnderTest.dropPrimaryKeyConstraint(getAdminConnection(), table(PRIMARY_KEY_TABLE));

        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final List<Column> actual = objectUnderTest.findAllPrimaryKeys(table(PRIMARY_KEY_TABLE));
            Assertions.assertThat(actual).isEmpty();
        });
    }

    @Test
    void shouldAddPrimaryKeyConstraint() throws SQLException {
        final com.ericsson.oss.air.pm.stats.common.model.collection.Table table = table(PRIMARY_KEY_TABLE);
        objectUnderTest.dropPrimaryKeyConstraint(getAdminConnection(), table);

        final Column column2 = Column.of("column2");
        final Column column4 = Column.of("column4");
        objectUnderTest.addPrimaryKeyConstraint(getAdminConnection(), table, Arrays.asList(column2, column4));

        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final List<Column> actual = objectUnderTest.findAllPrimaryKeys(table);
            Assertions.assertThat(actual).containsExactlyInAnyOrder(column2, column4);
        });
    }

    @Test
    void shouldReadColumnDefinitions() {
        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final List<ColumnDefinition> actual = objectUnderTest.findAllColumnDefinitions(TABLE_KPI_ROLLING_AGGREGATION_1440);
            Assertions.assertThat(actual)
                    .containsExactlyInAnyOrder(ColumnDefinition.of(Column.of("agg_column_0"), KpiDataType.POSTGRES_LONG),
                            ColumnDefinition.of(Column.of("aggregation_begin_time"), KpiDataType.POSTGRES_TIMESTAMP),
                            ColumnDefinition.of(Column.of("aggregation_end_time"), KpiDataType.POSTGRES_TIMESTAMP),
                            ColumnDefinition.of(Column.of("rolling_sum_integer_1440"), KpiDataType.POSTGRES_INTEGER),
                            ColumnDefinition.of(Column.of("rolling_max_integer_1440"), KpiDataType.POSTGRES_INTEGER));
        });
    }

    @Test
    void shouldFindColumnDefinition() {
        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final ColumnDefinition actual = objectUnderTest.findColumnDefinition(TABLE_KPI_ROLLING_AGGREGATION_1440,
                    "agg_column_0");
            Assertions.assertThat(actual).isEqualTo(ColumnDefinition.of(Column.of("agg_column_0"), KpiDataType.POSTGRES_LONG));
        });
    }

    @Test
    void shouldNotFindColumnDefinition() {
        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            assertThatThrownBy(() -> objectUnderTest.findColumnDefinition(TABLE_KPI_ROLLING_AGGREGATION_1440, "not_present"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Column 'not_present' is not found for table 'kpi_rolling_aggregation_1440'");
        });
    }

    @Test
    void shouldCreateSchema(){
        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            UUID collectionId = UUID.fromString("0f71a481-fdf1-487c-9ea6-480183149d97");
            objectUnderTest.createSchemaByCollectionId(collectionId);
            try(final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties());
                final Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(String.format("SELECT schema_name FROM information_schema.schemata"))){
                Set<String> schemas = new HashSet<>();
                while(rs.next()){
                  schemas.add(rs.getString(1));
                }
                Assertions.assertThat(schemas).contains("kpi_0f71a481-fdf1-487c-9ea6-480183149d97");
            }
        });
    }

    @Test
    void shouldHandleCheckedExceptionOnSchemaCreation(){
        DatabasePropertiesMock.prepare("wrongUrl", properties, ()->{
            assertThatThrownBy(()->objectUnderTest.createSchemaByCollectionId(UUID.randomUUID()))
                    .hasRootCauseInstanceOf(SQLException.class)
                    .isInstanceOf(UncheckedSqlException.class);
        });
    }

    @MethodSource("provideChangeDatatypeOfColumnData")
    @ParameterizedTest(name = "[{index}] Column: ''{0}'' type changed to: ''{1}''")
    void shouldChangeDatatypeOfColumn(final Column column, final KpiDataType kpiDataType) throws Exception {
        objectUnderTest.changeColumnType(getAdminConnection(),
                TABLE_PRIMARY_KEY,
                ColumnDefinition.of(column, kpiDataType));

        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final List<ColumnDefinition> actual = objectUnderTest.findAllColumnDefinitions(TABLE_PRIMARY_KEY);
            Assertions.assertThat(actual)
                    .filteredOn(columnDefinition -> columnDefinition.getColumn().equals(column))
                    .first()
                    .satisfies(columnDefinition -> {
                        Assertions.assertThat(columnDefinition.getDataType()).isEqualTo(kpiDataType);
                    });
        });
    }

    @Test
    void shouldCollectOutputTablesWithoutPartitions() {
        ScriptUtils.runInitScript(DATABASE_DELEGATE, "sql/container/create_partition_test_data.sql");

        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final List<String> actual = objectUnderTest.findAllOutputTablesWithoutPartition();
            Assertions.assertThat(actual).containsExactlyInAnyOrder("kpi_rolling_aggregation_1440", "kpi_60");
        });

        ScriptUtils.runInitScript(DATABASE_DELEGATE, "sql/container/drop_partition_test_data.sql");
    }

    @Test
    void shouldDeleteColumns() throws SQLException {
        final String tableName = TABLE_KPI_ROLLING_AGGREGATION_1440.getName();
        final Column column = Column.of("rolling_sum_integer_1440");
        final ColumnDefinition columnDefinition = ColumnDefinition.of(column, KpiDataType.POSTGRES_INTEGER);

        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final List<ColumnDefinition> actual = objectUnderTest.findAllColumnDefinitions(TABLE_KPI_ROLLING_AGGREGATION_1440);
            Assertions.assertThat(actual).contains(columnDefinition);
        });

        objectUnderTest.deleteColumnsForTable(getAdminConnection(), tableName, List.of(column.getName()));

        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            final List<ColumnDefinition> actual = objectUnderTest.findAllColumnDefinitions(TABLE_KPI_ROLLING_AGGREGATION_1440);
            Assertions.assertThat(actual).isNotEmpty().doesNotContain(columnDefinition);
        });
    }

    @Test
    @SneakyThrows
    void shouldDeleteTables() {
        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            Assertions.assertThat(objectUnderTest.doesTableExistByName(KPI_ROLLING_AGGREGATION_1440)).isTrue();
        });

        objectUnderTest.deleteTables(getAdminConnection(), List.of(KPI_ROLLING_AGGREGATION_1440));

        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            Assertions.assertThat(objectUnderTest.doesTableExistByName(KPI_ROLLING_AGGREGATION_1440)).isFalse();
        });
    }

    @ParameterizedTest
    @MethodSource("provideTabularParameter")
    void shouldSaveTabularParameter(final String header, final String data, final long expected) throws Exception {
        try (final Reader valueReader = new StringReader(data)) {
            final long actual = objectUnderTest.saveTabularParameter(getAdminConnection(), table("tabular_parameters_table"), header, valueReader);
            Assertions.assertThat(actual).isEqualTo(expected);
        }
    }

    @Test
    void shouldFailSavingTabularParameter() throws Exception {
        try (final Reader valueReader = new StringReader("calculation,two\nkpi,three")) {
            Assertions.assertThatExceptionOfType(SQLException.class).isThrownBy(() -> {
                objectUnderTest.saveTabularParameter(getAdminConnection(), table("tabular_parameters_table"), "\"name\",\"data\"", valueReader);
            });
        }
    }

    private static Stream<Arguments> provideChangeDatatypeOfColumnData() {
        final Column column = Column.of("column1");
        return Stream.of(Arguments.of(column, KpiDataType.POSTGRES_INTEGER),
                Arguments.of(column, KpiDataType.POSTGRES_FLOAT),
                Arguments.of(column, KpiDataType.POSTGRES_LONG),
                Arguments.of(column, KpiDataType.POSTGRES_REAL),
                Arguments.of(column, KpiDataType.POSTGRES_BOOLEAN),
                Arguments.of(column, KpiDataType.POSTGRES_STRING));
    }

    static Stream<Arguments> provideTabularParameter() {
        return Stream.of(
                Arguments.of("name , data", "calculation,2\nkpi,3", 2L),
                Arguments.of(null, "calculation,2\nkpi,3", 2L));
    }

    @Nested
    @DisplayName("should create output table")
    class ShouldCreateOutputTable {
        private static final String TEST_TABLE_60 = "test_table_60";

        @AfterEach
        void tearDown() {
            dropTable(TEST_TABLE_60);
        }

        @Test
        void nonDefaultOutputTable() throws Exception {
            final KpiDefinitionEntity definition = entity("testDefinition", "REAL", List.of("guid", "target_throughput_r"));

            try (final Connection connection = getAdminConnection()) {
                objectUnderTest.createOutputTable(connection, TableCreationInformation.of(TEST_TABLE_60, "60", Sets.newLinkedHashSet(definition)),
                        Map.of(
                                "guid", KpiDataType.POSTGRES_LONG,
                                "target_throughput_r", KpiDataType.POSTGRES_FLOAT));
            }

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                Assertions.assertThat(objectUnderTest.doesTableExistByName(TEST_TABLE_60)).isTrue();
            });

            assertThat(new Table(adminSource, getTableWithSchema(TEST_TABLE_60)))
                    .hasNumberOfColumns(5)
                    .column("GUID").isEmpty()                       // Aggregation element column
                    .column("TARGET_THROUGHPUT_R").isEmpty()        // Aggregation element column
                    .column("TESTDEFINITION").isEmpty()             // KPI Definition column
                    .column("AGGREGATION_BEGIN_TIME").isEmpty()     // Timestamp column
                    .column("AGGREGATION_END_TIME").isEmpty();      // Timestamp column

            assertThat(readPrimaryKeyRequest(TEST_TABLE_60)).isEmpty();
        }

        @Test
        void defaultOutputTable() throws Exception {
            final KpiDefinitionEntity definition = entity("testDefinition", "REAL", List.of("guid", "target_throughput_r"));

            try (final Connection connection = getAdminConnection()) {
                objectUnderTest.createOutputTable(connection, TableCreationInformation.of(TEST_TABLE_60, "-1", Sets.newLinkedHashSet(definition)),
                        Map.of(
                                "guid", KpiDataType.POSTGRES_LONG,
                                "target_throughput_r", KpiDataType.POSTGRES_FLOAT));
            }

            DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
                Assertions.assertThat(objectUnderTest.doesTableExistByName(TEST_TABLE_60)).isTrue();
            });

            assertThat(new Table(adminSource, getTableWithSchema(TEST_TABLE_60)))
                    .hasNumberOfColumns(3)
                    .column("GUID").isEmpty()                   // Aggregation element column
                    .column("TARGET_THROUGHPUT_R").isEmpty()    // Aggregation element column
                    .column("TESTDEFINITION").isEmpty();        // KPI Definition column

            assertThat(readPrimaryKeyRequest(TEST_TABLE_60))
                    .hasNumberOfRows(2)
                    .row().hasValues("guid", "bigint")
                    .row().hasValues("target_throughput_r", "double precision");
        }
    }

    @Nested
    @DisplayName("should modify output table")
    class ShouldModifyOutputTable {
        private static final String TEST_TABLE_1_440 = "test_table_1_440";

        @BeforeEach
        void setUp() throws SQLException {
            final KpiDefinitionEntity definition = entity("testDefinition", "REAL", List.of("guid", "target_throughput_r"));

            try (final Connection connection = getAdminConnection()) {
                objectUnderTest.createOutputTable(connection, TableCreationInformation.of(TEST_TABLE_1_440, "1_440", Sets.newLinkedHashSet(definition)),
                        Map.of(
                                "guid", KpiDataType.POSTGRES_LONG,
                                "target_throughput_r", KpiDataType.POSTGRES_FLOAT));
            }
        }

        @AfterEach
        void tearDown() {
            dropTable(TEST_TABLE_1_440);
        }

        @Test
        void shouldAddNewColumns() throws Exception {
            final KpiDefinitionEntity definition = entity("newDefinition", "BOOLEAN", List.of("fdn"));

            try (final Connection connection = getAdminConnection()) {
                objectUnderTest.addColumnsToOutputTable(connection,
                        TableCreationInformation.of(TEST_TABLE_1_440, "1_440", Sets.newLinkedHashSet(definition)),
                        Map.of("fdn", KpiDataType.POSTGRES_UNLIMITED_STRING));
            }

            assertThat(new Table(adminSource, getTableWithSchema(TEST_TABLE_1_440)))
                    .hasNumberOfColumns(7)
                    .column("GUID").isEmpty()                       // Aggregation element column
                    .column("TARGET_THROUGHPUT_R").isEmpty()        // Aggregation element column
                    .column("FDN").isEmpty()                        // Aggregation element column
                    .column("AGGREGATION_BEGIN_TIME").isEmpty()     // Timestamp columns
                    .column("AGGREGATION_END_TIME").isEmpty()       // Timestamp columns
                    .column("TESTDEFINITION").isEmpty()             // KPI Definition column
                    .column("NEWDEFINITION").isEmpty();             // KPI Definition column
        }
    }

    static Request readPrimaryKeyRequest(final String tableName) {
        return new Request(adminSource,
                "SELECT c.column_name, c.data_type " +
                        "FROM information_schema.table_constraints tc " +
                        "JOIN information_schema.constraint_column_usage AS ccu USING (constraint_schema, constraint_name) " +
                        "JOIN information_schema.columns AS c ON c.table_schema = tc.constraint_schema " +
                        " AND tc.table_name = c.table_name " +
                        " AND ccu.column_name = c.column_name " +
                        "WHERE constraint_type = 'PRIMARY KEY' " +
                        "  AND tc.table_name = ?",
                tableName);
    }

    static String getTableWithSchema(final String tableName) {
        return String.format("kpi.%s", tableName);
    }

    void dropTable(final String tableName) {
        executeUpdate(String.format("DROP TABLE %s", getTableWithSchema(tableName)));

        DatabasePropertiesMock.prepare(POSTGRE_SQL_CONTAINER.getJdbcUrl(), properties, () -> {
            Assertions.assertThat(objectUnderTest.doesTableExistByName(tableName)).isFalse();
        });
    }

    @SneakyThrows
    static void executeUpdate(final String sql) {
        try (final Connection connection = getAdminConnection();
             final Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    @SneakyThrows
    static Connection getAdminConnection() {
        return DriverManager.getConnection(POSTGRE_SQL_CONTAINER.getJdbcUrl(), POSTGRE_SQL_CONTAINER.getUsername(), POSTGRE_SQL_CONTAINER.getPassword());
    }

    static KpiDefinitionEntity entity(final String testDefinition, final String objectType, final List<String> aggregationElements) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(testDefinition);
        builder.withObjectType(objectType);
        builder.withAggregationElements(aggregationElements);
        return builder.build();
    }

    static com.ericsson.oss.air.pm.stats.common.model.collection.Table table(final String name) {
        return com.ericsson.oss.air.pm.stats.common.model.collection.Table.of(name);
    }

}