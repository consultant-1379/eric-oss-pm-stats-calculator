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

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;
import com.ericsson.oss.air.pm.stats.test.util.IntegrationTestUtils;

/**
 * POJO class representing the data needed to poll a database for an integration test.
 * <p>
 * Define the configuration using {@link DatabasePollingConfiguration.Builder} as follows:
 *
 * <pre>
 * final DatabasePollingConfiguration databasePollingConfiguration = new DatabasePollingConfiguration.Builder()
 *         .pollTableName(&quot;myTable&quot;)
 *         .forExpectedCount(150)
 *         .sleepFor(1, TimeUnit.MINUTES)
 *         .pollEvery(10, TimeUnit.SECONDS)
 *         .timeoutAfter(1, TimeUnit.MINUTES)
 *         .withJdbcDatasource(jdbcConnectionUrl, jdbcConnectionProperties)
 *         .build();
 * </pre>
 */
public class DatabasePollingConfiguration {

    private final long timeoutValueInNanoSeconds;
    private final long intervalValue;
    private final TimeUnit intervalUnit;
    private final long sleepValue;
    private final TimeUnit sleepUnit;
    private final String tableName;
    private final String conditionalClause;
    private final long expectedCount;
    private final JdbcDatasource jdbcDatasource;

    public DatabasePollingConfiguration(final long timeoutValueInNanoSeconds, final long intervalValue, final TimeUnit intervalUnit,
            final long sleepValue, final TimeUnit sleepUnit, final String tableName, final String conditionalClause, final long expectedCount,
            final JdbcDatasource jdbcDatasource) {
        this.timeoutValueInNanoSeconds = timeoutValueInNanoSeconds;
        this.intervalValue = intervalValue;
        this.intervalUnit = intervalUnit;
        this.sleepValue = sleepValue;
        this.sleepUnit = sleepUnit;
        this.tableName = tableName;
        this.conditionalClause = conditionalClause;
        this.expectedCount = expectedCount;
        this.jdbcDatasource = jdbcDatasource;
    }

    public long getTimeoutValueInNanoSeconds() {
        return timeoutValueInNanoSeconds;
    }

    public long getIntervalValue() {
        return intervalValue;
    }

    public TimeUnit getIntervalUnit() {
        return intervalUnit;
    }

    public String getTableName() {
        return tableName;
    }

    public String getConditionalClause() {
        return conditionalClause;
    }

    public long getExpectedCount() {
        return expectedCount;
    }

    public String getJdbcConnectionUrl() {
        return jdbcDatasource.getJbdcConnection();
    }

    public Properties getJdbcConnectionProperties() {
        return jdbcDatasource.getJdbcProperties();
    }

    /**
     * Sleeps for the configured amount of time.
     * <p>
     * If a sleep value/unit was not set through {@link DatabasePollingConfiguration.Builder}, no sleep will occur.
     *
     * @see IntegrationTestUtils#sleep(long, TimeUnit)
     */
    public void sleep() {
        if (sleepValue != 0) {
            IntegrationTestUtils.sleep(sleepValue, sleepUnit);
        }
    }

    /**
     * Builder class to create {@link DatabasePollingConfiguration} instances.
     */
    public static class Builder {

        private long timeoutValueInNanoSeconds;
        private long intervalValue;
        private TimeUnit intervalUnit;
        private long sleepValue;
        private TimeUnit sleepUnit = TimeUnit.SECONDS;
        private String tableName;
        private String conditionalClause = "";
        private long expectedCount;
        private JdbcDatasource jdbcDatasource;

        public Builder pollTableName(final String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder conditionalClause(final String conditionalClause) {
            this.conditionalClause = conditionalClause;
            return this;
        }

        public Builder forExpectedCount(final long expectedCount) {
            this.expectedCount = expectedCount;
            return this;
        }

        /**
         * Defines how long to sleep before beginning the database polling.
         * <p>
         * If not specified, default values of <code>0</code> {@link TimeUnit#SECONDS} will be used.
         *
         * @param sleepValue
         *            the value for the sleep
         * @param sleepUnit
         *            the {@link TimeUnit} for the sleep
         * @return the {@link Builder} instance
         */
        public Builder sleepFor(final long sleepValue, final TimeUnit sleepUnit) {
            this.sleepValue = sleepValue;
            this.sleepUnit = sleepUnit;
            return this;
        }

        /**
         * Defines the polling interval.
         *
         * @param intervalValue
         *            the value for the polling interval
         * @param intervalUnit
         *            the {@link TimeUnit} for the polling interval
         * @return the {@link Builder} instance
         */
        public Builder pollEvery(final long intervalValue, final TimeUnit intervalUnit) {
            this.intervalValue = intervalValue;
            this.intervalUnit = intervalUnit;
            return this;
        }

        /**
         * Defines the maximum timeout for the polling.
         *
         * @param timeoutValue
         *            the value for the timeout
         * @param timeoutUnit
         *            the {@link TimeUnit} for the timeout
         * @return the {@link Builder} instance
         */
        public Builder timeoutAfter(final long timeoutValue, final TimeUnit timeoutUnit) {
            timeoutValueInNanoSeconds = timeoutUnit.toNanos(timeoutValue);
            return this;
        }

        /**
         * Defines the JDBC connection details for the polling.
         *
         * @param jdbcDatasource
         *            the JDBC connection
         * @return the {@link Builder} instance
         */
        public Builder withJdbcDatasource(final JdbcDatasource jdbcDatasource) {
            this.jdbcDatasource = jdbcDatasource;
            return this;
        }

        public DatabasePollingConfiguration build() {
            return new DatabasePollingConfiguration(timeoutValueInNanoSeconds, intervalValue, intervalUnit, sleepValue, sleepUnit, tableName,
                    conditionalClause, expectedCount, jdbcDatasource);
        }
    }
}
