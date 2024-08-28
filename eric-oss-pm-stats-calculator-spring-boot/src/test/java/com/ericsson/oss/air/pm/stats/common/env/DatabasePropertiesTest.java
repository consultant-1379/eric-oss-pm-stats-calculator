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

import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;

class DatabasePropertiesTest {
    @Test
    void shouldReturnKpiServiceJdbcConnection() {
        try (final MockedStatic<Environment> environmentMockedStatic = Mockito.mockStatic(Environment.class)) {
            final String kpiServiceDbJdbcConnection = "KPI_SERVICE_DB_JDBC_CONNECTION";
            final String jdbcUrl = "jdbcUrl";

            final Verification verification = () -> Environment.getEnvironmentValue(kpiServiceDbJdbcConnection);
            environmentMockedStatic.when(verification).thenReturn(jdbcUrl);

            final String actual = DatabaseProperties.getKpiServiceJdbcConnection();

            environmentMockedStatic.verify(verification);

            Assertions.assertThat(actual).isEqualTo(jdbcUrl);
        }
    }

    @Test
    void shouldReturnKpiServiceJdbcServiceConnection() {
        try (final MockedStatic<Environment> environmentMockedStatic = Mockito.mockStatic(Environment.class)) {
            final String kpiServiceDbJdbcConnection = "KPI_SERVICE_DB_JDBC_SERVICE_CONNECTION";
            final String jdbcUrl = "jdbcUrl";

            final Verification verification = () -> Environment.getEnvironmentValue(kpiServiceDbJdbcConnection);
            environmentMockedStatic.when(verification).thenReturn(jdbcUrl);

            final String actual = DatabaseProperties.getKpiServiceJdbcServiceConnection();

            environmentMockedStatic.verify(verification);

            Assertions.assertThat(actual).isEqualTo(jdbcUrl);
        }
    }

    @Test
    void shouldReturnKpiServiceJdbcProperties() {
        try (final MockedStatic<Environment> environmentMockedStatic = Mockito.mockStatic(Environment.class)) {
            final String userValue = "userValue";
            final String passwordValue = "passwordValue";
            final String driverValue = "driverValue";

            final Verification userVerification = () -> Environment.getEnvironmentValue("KPI_SERVICE_DB_USER");
            final Verification passwordVerification = () -> Environment.getEnvironmentValue("KPI_SERVICE_DB_PASSWORD","");
            final Verification driverVerification = () -> Environment.getEnvironmentValue("KPI_SERVICE_DB_DRIVER");

            environmentMockedStatic.when(userVerification).thenReturn(userValue);
            environmentMockedStatic.when(passwordVerification).thenReturn(passwordValue);
            environmentMockedStatic.when(driverVerification).thenReturn(driverValue);

            final Properties actual = DatabaseProperties.getKpiServiceJdbcProperties();

            environmentMockedStatic.verify(userVerification);
            environmentMockedStatic.verify(passwordVerification);
            environmentMockedStatic.verify(driverVerification);

            final Properties expectedProperties = new Properties();
            expectedProperties.setProperty("user", userValue);
            expectedProperties.setProperty("password", passwordValue);
            expectedProperties.setProperty("driver", driverValue);

            Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expectedProperties);
        }
    }

    @Test
    void shouldReturnSuperUserJdbcProperties() {
        try (final MockedStatic<Environment> environmentMockedStatic = Mockito.mockStatic(Environment.class)) {
            final String userValue = "userValue";
            final String passwordValue = "passwordValue";
            final String driverValue = "driverValue";

            final Verification userVerification = () -> Environment.getEnvironmentValue("KPI_SERVICE_DB_ADMIN_USER");
            final Verification passwordVerification = () -> Environment.getEnvironmentValue("KPI_SERVICE_DB_ADMIN_PASSWORD","");
            final Verification driverVerification = () -> Environment.getEnvironmentValue("KPI_SERVICE_DB_DRIVER");

            environmentMockedStatic.when(userVerification).thenReturn(userValue);
            environmentMockedStatic.when(passwordVerification).thenReturn(passwordValue);
            environmentMockedStatic.when(driverVerification).thenReturn(driverValue);

            final Properties actual = DatabaseProperties.getSuperUserJdbcProperties();

            environmentMockedStatic.verify(userVerification);
            environmentMockedStatic.verify(passwordVerification);
            environmentMockedStatic.verify(driverVerification);

            final Properties expectedProperties = new Properties();
            expectedProperties.setProperty("user", userValue);
            expectedProperties.setProperty("password", passwordValue);
            expectedProperties.setProperty("driver", driverValue);

            Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expectedProperties);
        }
    }
}