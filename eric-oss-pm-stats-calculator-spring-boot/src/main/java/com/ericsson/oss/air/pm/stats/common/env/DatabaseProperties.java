/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.env;

import static com.ericsson.oss.air.pm.stats.common.env.Environment.getEnvironmentValue;

import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class which loads database credentials based off of environment variables.
 *
 * @see Environment#getEnvironmentValue(String)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseProperties {
    //  TODO: Create producer
    private static final String DRIVER_PROPERTY_KEY = "driver";
    private static final String PASSWORD_PROPERTY_KEY = "password";
    private static final String USER_PROPERTY_KEY = "user";

    /**
     * Retrieves the JDBC connection URL for <code>kpi_service_db</code> using the environment variables:
     * <ul>
     * <li>KPI_SERVICE_DB_JDBC_CONNECTION</li>
     * </ul>
     *
     * @return the JDBC connection URL for <code>kpi_service_db</code>
     */
    public static String getKpiServiceJdbcConnection() {
        return getEnvironmentValue("KPI_SERVICE_DB_JDBC_CONNECTION");
    }

    /**
     * Retrieves the JDBC service connection URL for <code>kpi_service_db</code> using the environment variables:
     * <ul>
     * <li>KPI_SERVICE_DB_JDBC_SERVICE_CONNECTION</li>
     * </ul>
     *
     * @return the JDBC service connection URL for <code>postgres</code>
     */
    public static String getKpiServiceJdbcServiceConnection() {
        return getEnvironmentValue("KPI_SERVICE_DB_JDBC_SERVICE_CONNECTION");
    }

    /**
     * Retrieves the JDBC connection properties ('user', 'password', 'driver') for <code>kpi_service_db</code> using the environment variables:
     * <ul>
     * <li>KPI_SERVICE_DB_USER</li>
     * <li>KPI_SERVICE_DB_PASSWORD</li>
     * <li>KPI_SERVICE_DB_DRIVER</li>
     * </ul>
     *
     * @return the JDBC connection properties for <code>kpi_service_db</code>
     */
    public static Properties getKpiServiceJdbcProperties() {
        final Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty(USER_PROPERTY_KEY, getEnvironmentValue("KPI_SERVICE_DB_USER"));
        jdbcProperties.setProperty(PASSWORD_PROPERTY_KEY, getEnvironmentValue("KPI_SERVICE_DB_PASSWORD", ""));
        jdbcProperties.setProperty(DRIVER_PROPERTY_KEY, getEnvironmentValue("KPI_SERVICE_DB_DRIVER"));
        return jdbcProperties;
    }


    /**
     * Retrieves the JDBC connection properties ('user', 'password', 'driver') for <code>postgres</code> using the environment variables:
     * <ul>
     * <li>KPI_SERVICE_DB_ADMIN_USER</li>
     * <li>KPI_SERVICE_DB_ADMIN_PASSWORD</li>
     * <li>KPI_SERVICE_DB_DRIVER</li>
     * </ul>
     *
     * @return the JDBC connection properties for <code>kpi_service_db</code>
     */
    public static Properties getSuperUserJdbcProperties() {
        final Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty(USER_PROPERTY_KEY, getEnvironmentValue("KPI_SERVICE_DB_ADMIN_USER"));
        jdbcProperties.setProperty(PASSWORD_PROPERTY_KEY, getEnvironmentValue("KPI_SERVICE_DB_ADMIN_PASSWORD", ""));
        jdbcProperties.setProperty(DRIVER_PROPERTY_KEY, getEnvironmentValue("KPI_SERVICE_DB_DRIVER"));
        return jdbcProperties;
    }
}
