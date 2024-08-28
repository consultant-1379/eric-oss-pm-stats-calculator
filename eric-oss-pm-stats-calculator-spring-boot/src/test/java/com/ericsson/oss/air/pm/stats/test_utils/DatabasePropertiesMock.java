/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test_utils;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockStatic;

import java.util.Properties;

import com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.mockito.MockedStatic;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabasePropertiesMock {

    @SneakyThrows
    public static void prepare(final String jdbcUrl, final Properties properties, final TestRunner testRunner) {
        try (final MockedStatic<DatabaseProperties> databasePropertiesMockedStatic = mockStatic(DatabaseProperties.class)) {
            databasePropertiesMockedStatic.when(DatabaseProperties::getKpiServiceJdbcConnection).thenReturn(jdbcUrl);
            databasePropertiesMockedStatic.when(DatabaseProperties::getKpiServiceJdbcProperties).thenReturn(properties);

            testRunner.run();

            databasePropertiesMockedStatic.verify(DatabaseProperties::getKpiServiceJdbcConnection, atLeastOnce());
            databasePropertiesMockedStatic.verify(DatabaseProperties::getKpiServiceJdbcProperties, atLeastOnce());
        }
    }

    @FunctionalInterface
    public interface TestRunner {
        void run() throws Exception;
    }
}
