/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
class DatasetUtilsTest {
    static final String H2_CONNECTION_URL_FORMAT = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
    static final String H2_CONNECTION_URL = String.format(H2_CONNECTION_URL_FORMAT, "testDb");
    static final String DB_USER = "administrator";
    static final String DB_PWD = "1234qwer";
    static final String DB_DRIVER = "org.h2.Driver";
    static final String RELATION_TABLE = "relation";
    static final String GUID = "guid";
    static final String ID = "id";

    static final Properties JDBC_PROPERTIES = new Properties();

    static SparkSession sparkSession;

    static {
        JDBC_PROPERTIES.setProperty("url", H2_CONNECTION_URL);
        JDBC_PROPERTIES.setProperty("dbtable", RELATION_TABLE);
        JDBC_PROPERTIES.setProperty("user", DB_USER);
        JDBC_PROPERTIES.setProperty("password", DB_PWD);
        JDBC_PROPERTIES.setProperty("driver", DB_DRIVER);
    }

    @BeforeAll
    static void beforeClass() {
        UserGroupInformation.setLoginUser(UserGroupInformation.createUserForTesting("hadoopTestUser", new String[]{"testGroup"}));
        sparkSession = SparkSession.builder().master("local[*]").appName("eric-oss-pm-stats-calculator-common-test").getOrCreate();
        createDb();
    }

    @AfterAll
    static void afterClass() {
        removeDataFromTable();
    }

    @Test
    void whenGetPartitionColumnLimitsIsCalled_andDataExistsInTable_thenExpectedMinAndMaxColumnValueLimitsAreReturned() throws SQLException {
        final ColumnValueLimits columnValueLimits = DatasetUtils.partitionLimits(sparkSession, RELATION_TABLE, ID, JDBC_PROPERTIES);
        assertEquals(7328661376176907531L, columnValueLimits.getMin());
        assertEquals(8035915982049115586L, columnValueLimits.getMax());
    }

    @Test
    void whenGetPartitionColumnLimitsIsCalled_andThePartitionColumnDoesNotExist_thenAnExceptionIsThrown() throws SQLException {
        assertThatThrownBy(() -> DatasetUtils.partitionLimits(sparkSession, RELATION_TABLE, GUID, JDBC_PROPERTIES))
                .isInstanceOf(SQLException.class);
    }

    static void createDb() {
        try (final Connection connection = getConnection()) {
            connection.setAutoCommit(true);

            final List<String> sqlCommands = getSqlCommands();
            for (final String sqlCommand : sqlCommands) {
                try (final PreparedStatement preparedStatement = connection.prepareStatement(sqlCommand)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (final Exception e) {
            fail("Unable to create db; reason: " + e.getMessage());
        }
    }

    static void removeDataFromTable() {
        try (final Connection connection = getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(String.format("DELETE FROM %s", RELATION_TABLE))) {
                preparedStatement.executeUpdate();
            }
        } catch (final SQLException e) {
            log.warn("Unable to delete table '{}'", RELATION_TABLE);
        }
    }

    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(H2_CONNECTION_URL, DB_USER, DB_PWD);
    }

    static List<String> getSqlCommands() throws IOException {
        return Files.readAllLines(Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "relation.sql"));
    }

}