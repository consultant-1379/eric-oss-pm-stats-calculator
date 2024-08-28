/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.SparkServiceImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.api.DatabasePropertiesProvider;
import com.ericsson.oss.air.pm.stats.calculator.service.util.api.ParameterParser;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.AggregationPeriodWindow;
import com.ericsson.oss.air.pm.stats.common.model.collection.Database;

import org.apache.spark.sql.SparkSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SparkServiceImplTest {
    @Mock ParameterParser parameterParserMock;
    @Mock DatabasePropertiesProvider databasePropertiesProviderMock;
    @Mock SparkSession sparkSessionMock;
    @Mock CalculationRepository calculationRepositoryMock;

    @InjectMocks @Spy SparkServiceImpl objectUnderTest;

    @Test
    void shouldGetComplexAggregationPeriodWindow() {
        doReturn(testTime(12)).when(objectUnderTest).getCalculationStartTime();
        doReturn(testTime(13)).when(objectUnderTest).getCalculationEndTime();

        final AggregationPeriodWindow actual = objectUnderTest.getComplexAggregationPeriodWindow();

        verify(objectUnderTest).getCalculationStartTime();
        verify(objectUnderTest).getCalculationEndTime();

        Assertions.assertThat(actual.getStart()).isEqualTo(Timestamp.valueOf(testTime(12)));
        Assertions.assertThat(actual.getEnd()).isEqualTo(Timestamp.valueOf(testTime(13)));
    }

    @Test
    void shouldConnectTo(@Mock final DatabaseProperties databasePropertiesMock, @Mock final Connection connectionMock) throws SQLException {
        final Database database = new Database("database");

        doReturn(databasePropertiesMock).when(objectUnderTest).getDatabaseProperties();
        doReturn(connectionMock).when(databasePropertiesMock).connectionTo(database);

        final Connection actual = objectUnderTest.connectionTo(database);

        verify(objectUnderTest).getDatabaseProperties();
        verify(databasePropertiesMock).connectionTo(database);

        Assertions.assertThat(actual).isEqualTo(connectionMock);
    }

    static LocalDateTime testTime(final int hour) {
        return LocalDateTime.of(2_022, Month.NOVEMBER, 11, hour, 0);
    }
}