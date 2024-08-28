/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatabaseProperties;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.Calculation;
import com.ericsson.oss.air.pm.stats.calculator.model.exception.SparkPropertyMissingException;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.CalculationRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.util.api.DatabasePropertiesProvider;
import com.ericsson.oss.air.pm.stats.calculator.service.util.api.ParameterParser;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalog.Catalog;
import org.apache.spark.sql.internal.SQLConf;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SparkServiceImplTest {
    @Mock ParameterParser parameterParserMock;
    @Mock DatabasePropertiesProvider databasePropertiesProviderMock;
    @Mock SparkSession sparkSessionMock;
    @Mock CalculationRepository calculationRepositoryMock;

    @InjectMocks SparkServiceImpl objectUnderTest;

    @Test
    void getCalculationId() {
        mockConfiguration(sparkConfMock -> {
            when(sparkConfMock.get("spark.calculationId")).thenReturn("2cf8cd94-dec5-45c2-96ec-3eff6a4302f0");

            final UUID actual = objectUnderTest.getCalculationId();

            verify(sparkConfMock).get("spark.calculationId");

            Assertions.assertThat(actual).isEqualTo(UUID.fromString("2cf8cd94-dec5-45c2-96ec-3eff6a4302f0"));
        });
    }

    @Test
    void getCalculationEndTime() {
        mockConfiguration(sparkConfMock -> {
            final LocalDateTime testTime = LocalDateTime.of(2_022, Month.FEBRUARY, 16, 18, 31);
            when(sparkConfMock.get("spark.calculationEndTime")).thenReturn(String.valueOf(testTime));

            final LocalDateTime actual = objectUnderTest.getCalculationEndTime();

            verify(sparkConfMock).get("spark.calculationEndTime");

            Assertions.assertThat(actual).isEqualTo(testTime);
        });
    }

    @Test
    void shouldRaiseException_whenCalculationEndTimeIsMissing() {
        mockConfiguration(sparkConfMock -> {
            Assertions.assertThatThrownBy(() -> objectUnderTest.getCalculationEndTime())
                      .isInstanceOf(SparkPropertyMissingException.class)
                      .hasMessage("spark.calculationEndTime attribute is missing");
        });
    }

    @Test
    void getCalculationStartTime() {
        mockConfiguration(sparkConfMock -> {
            final LocalDateTime testTime = LocalDateTime.of(2_022, Month.FEBRUARY, 16, 18, 31);
            when(sparkConfMock.get("spark.calculationStartTime")).thenReturn(String.valueOf(testTime));

            final LocalDateTime actual = objectUnderTest.getCalculationStartTime();

            verify(sparkConfMock).get("spark.calculationStartTime");

            Assertions.assertThat(actual).isEqualTo(testTime);
        });
    }

    @Test
    void shouldRaiseException_whenCalculationStartTimeIsMissing() {
        mockConfiguration(sparkConfMock -> {
            Assertions.assertThatThrownBy(() -> objectUnderTest.getCalculationStartTime())
                      .isInstanceOf(SparkPropertyMissingException.class)
                      .hasMessage("spark.calculationStartTime attribute is missing");
        });
    }

    @Test
    void getKpisToCalculate() {
        mockConfiguration(sparkConfMock -> {
            when(sparkConfMock.get("spark.kpisToCalculate")).thenReturn("kpi1,kpi2,kpi3");

            final List<String> actual = objectUnderTest.getKpisToCalculate();

            verify(sparkConfMock).get("spark.kpisToCalculate");

            Assertions.assertThat(actual).containsExactlyInAnyOrder("kpi1", "kpi2", "kpi3");
        });
    }

    @Test
    void getExecutionGroup() {
        mockConfiguration(sparkConfMock -> {
            when(sparkConfMock.get("spark.executionGroup")).thenReturn("executionGroup");

            final String actual = objectUnderTest.getExecutionGroup();

            verify(sparkConfMock).get("spark.executionGroup");

            Assertions.assertThat(actual).isEqualTo("executionGroup");
        });
    }

    @Test
    void getKpiJdbcConnection() {
        mockConfiguration(sparkConfMock -> {
            when(sparkConfMock.get("spark.jdbc.kpi.jdbcUrl")).thenReturn("jdbc:postgres");

            final String actual = objectUnderTest.getKpiJdbcConnection();

            verify(sparkConfMock).get("spark.jdbc.kpi.jdbcUrl");

            Assertions.assertThat(actual).isEqualTo("jdbc:postgres");
        });
    }

    @Test
    void getKpiJdbcProperties() {
        mockConfiguration(sparkConfMock -> {
            when(sparkConfMock.get("spark.jdbc.kpi.user")).thenReturn("user");
            when(sparkConfMock.get("spark.jdbc.kpi.password")).thenReturn("password");
            when(sparkConfMock.get("spark.jdbc.kpi.driverClass")).thenReturn("driverClass");
            when(sparkConfMock.get("spark.jdbc.kpi.type")).thenReturn("FACT");
            when(sparkConfMock.get("spark.jdbc.kpi.expressionTag")).thenReturn("kpi_db");

            final Properties actual = objectUnderTest.getKpiJdbcProperties();

            verify(sparkConfMock).get("spark.jdbc.kpi.user");
            verify(sparkConfMock).get("spark.jdbc.kpi.password");
            verify(sparkConfMock).get("spark.jdbc.kpi.driverClass");
            verify(sparkConfMock).get("spark.jdbc.kpi.type");
            verify(sparkConfMock).get("spark.jdbc.kpi.expressionTag");

            final Properties expected = new Properties();
            expected.setProperty("user", "user");
            expected.setProperty("password", "password");
            expected.setProperty("driver", "driverClass");
            expected.setProperty("type", "FACT");
            expected.setProperty("expressionTag", "kpi_db");

            Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
        });
    }

    @Test
    void getKpiDatabaseDatasource() {
        final Datasource actual = objectUnderTest.getKpiDatabaseDatasource();

        Assertions.assertThat(actual).isEqualTo(Datasource.KPI_DB);
    }

    @Test
    void getKpiJdbcDatasource() {
        mockConfiguration(sparkConfMock -> {
            when(sparkConfMock.get("spark.jdbc.kpi.jdbcUrl")).thenReturn("jdbc:postgres");
            when(sparkConfMock.get("spark.jdbc.kpi.user")).thenReturn("user");
            when(sparkConfMock.get("spark.jdbc.kpi.password")).thenReturn("password");
            when(sparkConfMock.get("spark.jdbc.kpi.driverClass")).thenReturn("driverClass");
            when(sparkConfMock.get("spark.jdbc.kpi.type")).thenReturn("FACT");
            when(sparkConfMock.get("spark.jdbc.kpi.expressionTag")).thenReturn("kpi_db");

            final JdbcDatasource actual = objectUnderTest.getKpiJdbcDatasource();

            verify(sparkConfMock).get("spark.jdbc.kpi.jdbcUrl");
            verify(sparkConfMock).get("spark.jdbc.kpi.user");
            verify(sparkConfMock).get("spark.jdbc.kpi.password");
            verify(sparkConfMock).get("spark.jdbc.kpi.driverClass");
            verify(sparkConfMock).get("spark.jdbc.kpi.type");
            verify(sparkConfMock).get("spark.jdbc.kpi.expressionTag");

            final Properties expected = new Properties();
            expected.setProperty("user", "user");
            expected.setProperty("password", "password");
            expected.setProperty("driver", "driverClass");
            expected.setProperty("type", "FACT");
            expected.setProperty("expressionTag", "kpi_db");

            Assertions.assertThat(actual.getJbdcConnection()).isEqualTo("jdbc:postgres");
            Assertions.assertThat(actual.getJdbcProperties()).containsExactlyInAnyOrderEntriesOf(expected);
        });
    }

    @Test
    void getApplicationId() {
        final SparkContext sparkContextMock = mock(SparkContext.class);

        when(sparkSessionMock.sparkContext()).thenReturn(sparkContextMock);
        when(sparkContextMock.applicationId()).thenReturn("applicationId");

        final String actual = objectUnderTest.getApplicationId();

        verify(sparkSessionMock).sparkContext();
        verify(sparkContextMock).applicationId();

        Assertions.assertThat(actual).isEqualTo("applicationId");
    }

    @MethodSource("provideIsSimpleData")
    @ParameterizedTest
    void isScheduledSimple(final KpiType kpiType, final boolean expected) {
        final Calculation calculationMock = mock(Calculation.class);
        mockConfiguration(sparkConfMock -> {
            final UUID calculationId = UUID.fromString("2cf8cd94-dec5-45c2-96ec-3eff6a4302f0");
            when(sparkConfMock.get("spark.calculationId")).thenReturn(calculationId.toString());
            when(calculationRepositoryMock.forceFetchById(calculationId)).thenReturn(calculationMock);
            when(calculationMock.getKpiType()).thenReturn(kpiType);

            final boolean actual = objectUnderTest.isScheduledSimple();

            verify(calculationRepositoryMock).forceFetchById(any());
            verify(calculationMock).getKpiType();

            Assertions.assertThat(actual).isEqualTo(expected);
        });
    }

    @MethodSource("provideIsComplexData")
    @ParameterizedTest
    void isScheduledComplex(final KpiType kpiType, final boolean expected) {
        final Calculation calculationMock = mock(Calculation.class);
        mockConfiguration(sparkConfMock -> {
            final UUID calculationId = UUID.fromString("2cf8cd94-dec5-45c2-96ec-3eff6a4302f0");
            when(sparkConfMock.get("spark.calculationId")).thenReturn(calculationId.toString());
            when(calculationRepositoryMock.forceFetchById(calculationId)).thenReturn(calculationMock);
            when(calculationMock.getKpiType()).thenReturn(kpiType);

            final boolean actual = objectUnderTest.isScheduledComplex();

            verify(calculationRepositoryMock).forceFetchById(any());
            verify(calculationMock).getKpiType();

            Assertions.assertThat(actual).isEqualTo(expected);
        });
    }

    @Test
    void readDatabaseProperties() {
        mockConfiguration(sparkConfMock -> {
            final DatabaseProperties databaseProperties = DatabaseProperties.newInstance();

            when(databasePropertiesProviderMock.readDatabaseProperties(sparkConfMock)).thenReturn(databaseProperties);

            final DatabaseProperties actual = objectUnderTest.getDatabaseProperties();

            verify(databasePropertiesProviderMock).readDatabaseProperties(sparkConfMock);

            Assertions.assertThat(actual).isEqualTo(databaseProperties);
        });
    }

   @Test
   void shouldCacheView(@Mock final Dataset<Row> datasetMock, @Mock final Catalog catalogMock) {
       when(sparkSessionMock.catalog()).thenReturn(catalogMock);

       final TableDataset actual = objectUnderTest.cacheView("viewName", datasetMock);

       final InOrder inOrder = inOrder(sparkSessionMock, datasetMock, catalogMock);
       inOrder.verify(datasetMock).cache();
       inOrder.verify(sparkSessionMock).catalog();
       inOrder.verify(catalogMock).dropTempView("viewName");
       inOrder.verify(datasetMock).createOrReplaceTempView("viewName");

       Assertions.assertThat(actual.getTable()).isEqualTo(Table.of("viewName"));
       Assertions.assertThat(actual.getDataset()).isEqualTo(datasetMock);
   }

    @Test
    void shouldClearCache(@Mock final SQLContext sqlContextMock) {
        when(sparkSessionMock.sqlContext()).thenReturn(sqlContextMock);

        objectUnderTest.clearCache();

        verify(sparkSessionMock).sqlContext();
        verify(sqlContextMock).clearCache();
    }

    @Nested
    @DisplayName("Job Description register-unregister")
    class JobDescription {
        @Mock SQLContext sqlContextMock;
        @Mock SQLConf sqlConfMock;

        @Test
        void shouldRegisterJobDescription() {
            final String description = "Description";

            when(sparkSessionMock.sqlContext()).thenReturn(sqlContextMock);
            when(sqlContextMock.conf()).thenReturn(sqlConfMock);

            objectUnderTest.registerJobDescription(description);

            verify(sparkSessionMock).sqlContext();
            verify(sqlContextMock).conf();
            verify(sqlConfMock).setConfString("spark.ericsson.job.description", description);
            verify(sqlConfMock).setConfString("spark.job.description", description);
        }

        @Test
        void shouldUnregisterJobDescription() {
            when(sparkSessionMock.sqlContext()).thenReturn(sqlContextMock);
            when(sqlContextMock.conf()).thenReturn(sqlConfMock);

            objectUnderTest.unregisterJobDescription();

            verify(sparkSessionMock).sqlContext();
            verify(sqlContextMock).conf();
            verify(sqlConfMock).unsetConf("spark.ericsson.job.description");
            verify(sqlConfMock).unsetConf("spark.job.description");
        }
    }

    @Test
    void shouldGetKpiDefinitionParameters(@Mock final Calculation calculationMock, @Mock final Map<String, String> parsedParametersMock) {
        mockConfiguration(sparkConfMock -> {
            final UUID calculationId = UUID.fromString("2cf8cd94-dec5-45c2-96ec-3eff6a4302f0");
            when(sparkConfMock.get("spark.calculationId")).thenReturn(calculationId.toString());
            when(calculationRepositoryMock.forceFetchById(calculationId)).thenReturn(calculationMock);
            when(calculationMock.getParameters()).thenReturn("parameterJSON");
            when(parameterParserMock.parseParameters("parameterJSON")).thenReturn(parsedParametersMock);

            final Map<String, String> actual = objectUnderTest.getKpiDefinitionParameters();

            verify(sparkConfMock).get("spark.calculationId");
            verify(calculationRepositoryMock).forceFetchById(calculationId);
            verify(calculationMock).getParameters();
            verify(parameterParserMock).parseParameters("parameterJSON");

            Assertions.assertThat(actual).isEqualTo(parsedParametersMock);
        });
    }

    @Nested
    class FromKafka {
        @Mock DataFrameReader dataFrameReaderMock;

        @Test
        void shouldPrepareToReadFromKafka() {
            when(sparkSessionMock.read()).thenReturn(dataFrameReaderMock);
            when(dataFrameReaderMock.format("kafka")).thenReturn(dataFrameReaderMock);

            final DataFrameReader actual = objectUnderTest.fromKafka();

            verify(sparkSessionMock).read();
            verify(dataFrameReaderMock).format("kafka");

            Assertions.assertThat(actual).isEqualTo(dataFrameReaderMock);
        }
    }

    @MethodSource("provideExecutionGroup")
    @ParameterizedTest(name = "[{index}] ''{0}'' executionGroup is on demand: ''{1}''")
    void isOnDemand(final String executionGroup, final boolean expected) {
        mockConfiguration(sparkConfMock -> {
            when(sparkConfMock.get("spark.executionGroup")).thenReturn(executionGroup);

            final boolean actual = objectUnderTest.isOnDemand();

            verify(sparkConfMock).get("spark.executionGroup");

            Assertions.assertThat(actual).isEqualTo(expected);
        });
    }

    static Stream<Arguments> provideExecutionGroup() {
        return Stream.of(
                Arguments.of("Scheduled", false),
                Arguments.of("ON_DEMAND", true)
        );
    }

    static Stream<Arguments> provideIsSimpleData() {
        return Stream.of(
                Arguments.of(KpiType.ON_DEMAND, false),
                Arguments.of(KpiType.SCHEDULED_COMPLEX, false),
                Arguments.of(KpiType.SCHEDULED_SIMPLE, true)
        );
    }

    static Stream<Arguments> provideIsComplexData() {
        return Stream.of(
                Arguments.of(KpiType.ON_DEMAND, false),
                Arguments.of(KpiType.SCHEDULED_COMPLEX, true),
                Arguments.of(KpiType.SCHEDULED_SIMPLE, false)
        );
    }

    void mockConfiguration(final ConfigurationRunner configurationRunner) {
        final SparkContext sparkContextMock = mock(SparkContext.class);
        final SparkConf sparkConfMock = mock(SparkConf.class);

        when(sparkSessionMock.sparkContext()).thenReturn(sparkContextMock);
        when(sparkContextMock.getConf()).thenReturn(sparkConfMock);

        configurationRunner.run(sparkConfMock);

        verify(sparkSessionMock, atLeastOnce()).sparkContext();
        verify(sparkContextMock, atLeastOnce()).getConf();
    }

    private interface ConfigurationRunner {
        void run(SparkConf sparkConfMock);
    }
}