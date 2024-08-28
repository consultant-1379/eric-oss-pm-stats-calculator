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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DriverManagerMock {
    private static final String JDBC_URL = "jdbcUrl";

    @SneakyThrows
    public static void prepareThrow(final Class<? extends Throwable> throwableType, final TestRunner testRunner) {
        try (final MockedStatic<DriverManager> driverManagerMockedStatic = mockStatic(DriverManager.class)) {
            final Properties properties = new Properties();
            final Connection connectionMock = mock(Connection.class);

            final Verification verification = () -> DriverManager.getConnection(JDBC_URL, properties);
            driverManagerMockedStatic.when(verification).thenThrow(throwableType);

            DatabasePropertiesMock.prepare(JDBC_URL, properties, () -> {
                testRunner.run(connectionMock);
            });

            driverManagerMockedStatic.verify(verification);
        }
    }

    @SneakyThrows
    public static void prepare(final TestRunner testRunner) {
        try (final MockedStatic<DriverManager> driverManagerMockedStatic = mockStatic(DriverManager.class)) {
            final Properties properties = new Properties();
            final Connection connectionMock = mock(Connection.class);

            final Verification verification = () -> DriverManager.getConnection(JDBC_URL, properties);
            driverManagerMockedStatic.when(verification).thenReturn(connectionMock);

            DatabasePropertiesMock.prepare(JDBC_URL, properties, () -> {
                testRunner.run(connectionMock);
            });

            driverManagerMockedStatic.verify(verification);
        }
    }

    @FunctionalInterface
    public interface TestRunner {
        void run(Connection connectionMock) throws Exception;
    }
}
