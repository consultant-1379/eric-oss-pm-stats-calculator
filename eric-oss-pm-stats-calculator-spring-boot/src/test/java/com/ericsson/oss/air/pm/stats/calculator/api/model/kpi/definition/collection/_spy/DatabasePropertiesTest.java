/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabasePropertiesTest {
    @Spy
    DatabaseProperties objectUnderTest;

    @Test
    void shouldVerifyConnectTo(@Mock final Database databaseMock,
                               @Mock final Properties propertiesMock,
                               @Mock final Connection connectionMock) throws SQLException {
        try (final MockedStatic<DriverManager> driverManagerMockedStatic = mockStatic(DriverManager.class)) {
            final Verification verification = () -> DriverManager.getConnection("jdbcUrl", propertiesMock);

            doReturn("jdbcUrl").when(objectUnderTest).getDatabaseJdbcUrl(databaseMock);
            doReturn(propertiesMock).when(objectUnderTest).getDatabaseProperties(databaseMock);
            driverManagerMockedStatic.when(verification).thenReturn(connectionMock);

            final Connection actual = objectUnderTest.connectionTo(databaseMock);

            verify(objectUnderTest).getDatabaseJdbcUrl(databaseMock);
            verify(objectUnderTest).getDatabaseProperties(databaseMock);
            driverManagerMockedStatic.verify(verification);

            Assertions.assertThat(actual).isEqualTo(connectionMock);
        }
    }
}