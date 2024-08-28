/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util.sql;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static com.ericsson.oss.air.pm.stats.test.util.IntegrationTestUtils.sleep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;
import com.ericsson.oss.air.pm.stats.test.util.sql.DatabasePollingConfiguration.Builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class to poll a database for integration tests.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabasePollUtils {

    public static void pollDatabaseForExpectedValue(@NonNull final Function<? super Builder, DatabasePollingConfiguration> configurationFunction) {
        final Builder builder = new Builder();
        builder.sleepFor(0, TimeUnit.SECONDS);
        builder.pollEvery(1, TimeUnit.SECONDS);
        builder.timeoutAfter(1, TimeUnit.MINUTES);
        builder.withJdbcDatasource(new JdbcDatasource(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties()));
        configurationFunction.apply(builder);
    }

    /**
     * Polls a database table for an integration test. The provided {@link DatabasePollingConfiguration} will define the table to check, and a SQL
     * <b>COUNT</b> will be performed, in the form:
     *
     * <pre>
     *     SELECT COUNT(*) FROM {@literal <}tableName{@literal >}
     * </pre>
     *
     * If the {@link DatabasePollingConfiguration#sleep()} has been configured through the {@link DatabasePollingConfiguration.Builder}, then the
     * polling will only start after a sleep.If the returned count is not equal to the {@link DatabasePollingConfiguration#getExpectedCount()}, the
     * thread will sleep for {@link DatabasePollingConfiguration#getIntervalValue()} {@link DatabasePollingConfiguration#getIntervalUnit()}s. This
     * will continue until the {@link DatabasePollingConfiguration#getTimeoutValueInNanoSeconds()} value has been exceeded.
     *
     * @param databasePollingConfiguration
     *            the configuration for the database polling
     * @throws AssertionError
     *             thrown if the {@link DatabasePollingConfiguration#getTimeoutValueInNanoSeconds()} is exceeded without a value count being retrieved
     *             from the database
     */
    public static void pollDatabaseForExpectedValue(final DatabasePollingConfiguration databasePollingConfiguration) {
        final String sqlCountQuery = String.format("SELECT COUNT(*) FROM \"%1$s\" %2$s;", databasePollingConfiguration.getTableName(),
                databasePollingConfiguration.getConditionalClause());
        databasePollingConfiguration.sleep();

        try (final Connection connection = DriverManager.getConnection(databasePollingConfiguration.getJdbcConnectionUrl(),
                databasePollingConfiguration.getJdbcConnectionProperties());
                final Statement statement = connection.createStatement()) {
            final long startTime = System.nanoTime();
            if (pollUntilMatchOrTimeout(statement, databasePollingConfiguration, startTime, sqlCountQuery)) {
                return;
            }

            throw new AssertionError(
                    String.format("Database polling of '%s' timed out after %s seconds", databasePollingConfiguration.getTableName(),
                            TimeUnit.NANOSECONDS.toSeconds(databasePollingConfiguration.getTimeoutValueInNanoSeconds())));
        } catch (final SQLException e) {
            throw new AssertionError(String.format("Error opening connection to poll database with query '%s'", sqlCountQuery), e);
        }
    }

    private static boolean pollUntilMatchOrTimeout(final Statement statement, final DatabasePollingConfiguration databasePollingConfiguration,
            final long startTime, final String sqlCountQuery) {
        while (System.nanoTime() - startTime < databasePollingConfiguration.getTimeoutValueInNanoSeconds()) {
            final int count = getCount(statement, sqlCountQuery);

            if (databasePollingConfiguration.getExpectedCount() == count) {
                log.info("Found expected value in table '{}': {}", databasePollingConfiguration.getTableName(), count);
                return true;
            }

            log.warn("Wanted value {}, found {}", databasePollingConfiguration.getExpectedCount(), count);
            sleep(databasePollingConfiguration.getIntervalValue(), databasePollingConfiguration.getIntervalUnit());
        }

        return false;
    }

    private static int getCount(final Statement statement, final String sqlCountQuery) {
        try (final ResultSet resultSet = statement.executeQuery(sqlCountQuery)) {
            resultSet.next();
            return resultSet.getInt("count");
        } catch (final Exception e) {
            log.warn("Error getting count for SQL query '{}'", sqlCountQuery, e);
            return -1;
        }
    }
}
