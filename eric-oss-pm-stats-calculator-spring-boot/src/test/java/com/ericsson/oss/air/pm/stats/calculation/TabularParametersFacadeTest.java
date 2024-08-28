/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation;

import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;
import com.ericsson.oss.air.pm.stats.model.exception.TabularParameterValidationException;
import com.ericsson.oss.air.pm.stats.model.exception.UncheckedSqlException;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.DimensionTablesService;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TabularParametersFacadeTest {
    static UUID CALCULATION_ID = UUID.fromString("b2531c89-8513-4a88-aa1a-b99484321628");

    @Mock
    DatabaseService databaseServiceMock;
    @Mock
    DimensionTablesService dimensionTablesServiceMock;

    @InjectMocks
    TabularParameterFacade objectUnderTest;

    @Test
    void shouldDeleteTablesForOnDemand(@Mock KpiCalculationJob kpiCalculationJobMock) {
        final List<String> tableNames = List.of("table_1", "table_2");

        DriverManagerMock.prepare(connectionMock -> {
            when(kpiCalculationJobMock.isOnDemand()).thenReturn(true);
            when(kpiCalculationJobMock.getCalculationId()).thenReturn(CALCULATION_ID);
            when(dimensionTablesServiceMock.findTableNamesForCalculation(CALCULATION_ID)).thenReturn(tableNames);

            objectUnderTest.deleteTables(kpiCalculationJobMock);

            verify(kpiCalculationJobMock).isOnDemand();
            verify(kpiCalculationJobMock).getCalculationId();
            verify(dimensionTablesServiceMock).findTableNamesForCalculation(CALCULATION_ID);
            verify(databaseServiceMock).deleteTables(connectionMock, tableNames);
        });
    }

    @Test
    void shouldDoNothingForScheduled(@Mock KpiCalculationJob kpiCalculationJobMock) {
        when(kpiCalculationJobMock.isOnDemand()).thenReturn(false);

        objectUnderTest.deleteTables(kpiCalculationJobMock);

        verify(kpiCalculationJobMock).isOnDemand();
        verifyNoMoreInteractions(kpiCalculationJobMock);
        verifyNoInteractions(databaseServiceMock, dimensionTablesServiceMock);
    }

    @Test
    void shouldThrowUncheckedSqlOnDeleteTables(@Mock KpiCalculationJob kpiCalculationJobMock) {
        final List<String> tableNames = List.of("table_1", "table_2");

        DriverManagerMock.prepare(connectionMock -> {
            when(kpiCalculationJobMock.isOnDemand()).thenReturn(true);
            when(kpiCalculationJobMock.getCalculationId()).thenReturn(CALCULATION_ID);
            when(dimensionTablesServiceMock.findTableNamesForCalculation(CALCULATION_ID)).thenReturn(tableNames);
            doThrow(SQLException.class).when(databaseServiceMock).deleteTables(connectionMock, tableNames);

            Assertions.assertThatThrownBy(() -> objectUnderTest.deleteTables(kpiCalculationJobMock))
                    .hasRootCauseExactlyInstanceOf(SQLException.class)
                    .isInstanceOf(UncheckedSqlException.class);

            verify(kpiCalculationJobMock).isOnDemand();
            verify(kpiCalculationJobMock, times(2)).getCalculationId();
            verify(dimensionTablesServiceMock).findTableNamesForCalculation(CALCULATION_ID);
            verify(databaseServiceMock).deleteTables(connectionMock, tableNames);
        });
    }

    @Test
    void shouldNotDeleteWhenNoTabularParameters(@Mock KpiCalculationJob kpiCalculationJobMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiCalculationJobMock.isOnDemand()).thenReturn(true);
            when(kpiCalculationJobMock.getCalculationId()).thenReturn(CALCULATION_ID);
            when(dimensionTablesServiceMock.findTableNamesForCalculation(CALCULATION_ID)).thenReturn(List.of());

            objectUnderTest.deleteTables(kpiCalculationJobMock);

            verify(kpiCalculationJobMock).isOnDemand();
            verify(kpiCalculationJobMock).getCalculationId();
            verify(dimensionTablesServiceMock).findTableNamesForCalculation(CALCULATION_ID);
            verifyNoInteractions(databaseServiceMock);
        });
    }

    @Test
    void shouldThrowExceptionOnSaveTabularParameters_whenCreateTabularParameterTablesFailed() {
        final TabularParameters tabularParameter = TabularParameters.builder().name("tabularParameter").build();

        DriverManagerMock.prepare(connectionMock -> {

            doThrow(SQLException.class).when(databaseServiceMock).createTabularParameterTables(connectionMock, List.of("tabularParameter"), CALCULATION_ID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.saveTabularParameterTables(CALCULATION_ID, List.of(tabularParameter)))
                    .hasRootCauseExactlyInstanceOf(SQLException.class)
                    .isInstanceOf(KpiPersistenceException.class)
                    .hasMessage("Unable to save tabular parameters");

            verify(databaseServiceMock).createTabularParameterTables(connectionMock, List.of("tabularParameter"), CALCULATION_ID);
            verifyNoMoreInteractions(databaseServiceMock);
        });
    }

    @Test
    void shouldThrowExceptionOnSaveTabularParameters_whenSaveTabularParameterFailed() {
        final TabularParameters tabularParameter = TabularParameters.builder().name("tabularParameter").build();

        DriverManagerMock.prepare(connectionMock -> {

            doThrow(SQLException.class).when(databaseServiceMock).saveTabularParameter(connectionMock, tabularParameter, CALCULATION_ID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.saveTabularParameterTables(CALCULATION_ID, List.of(tabularParameter)))
                    .hasRootCauseExactlyInstanceOf(SQLException.class)
                    .isInstanceOf(TabularParameterValidationException.class)
                    .hasMessage("Unable to save tabular parameters");

            verify(databaseServiceMock).createTabularParameterTables(connectionMock, List.of("tabularParameter"), CALCULATION_ID);
            verify(databaseServiceMock).saveTabularParameter(connectionMock, tabularParameter, CALCULATION_ID);
            verify(databaseServiceMock).deleteTables(connectionMock, List.of("tabularParameter_b2531c89"));
        });
    }

    @Test
    void shouldThrowExceptionOnSaveTabularParameterDimensions() {
        final TabularParameters tabularParameter = TabularParameters.builder().name("tabularParameter").build();

        DriverManagerMock.prepare(connectionMock -> {

            doThrow(SQLException.class).when(dimensionTablesServiceMock).save(connectionMock, List.of("tabularParameter_b2531c89"), CALCULATION_ID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.saveTabularParameterDimensions(CALCULATION_ID, List.of(tabularParameter)))
                    .hasRootCauseExactlyInstanceOf(SQLException.class)
                    .isInstanceOf(KpiPersistenceException.class)
                    .hasMessage("Unable to save tabular parameters");

            verify(dimensionTablesServiceMock).save(connectionMock, List.of("tabularParameter_b2531c89"), CALCULATION_ID);
        });
    }

    @Test
    void shouldDoNothingWhenTabularParametersListEmpty() {
        objectUnderTest.saveTabularParameterTables(CALCULATION_ID, List.of());

        verifyNoInteractions(databaseServiceMock, dimensionTablesServiceMock);
    }

    @Test
    void shouldSaveTabularParameterTables() {
        final TabularParameters tabularParameters = TabularParameters.builder().name("tabularParameters").build();

        DriverManagerMock.prepare(connectionMock -> {
            objectUnderTest.saveTabularParameterTables(CALCULATION_ID, List.of(tabularParameters));

            verify(databaseServiceMock).createTabularParameterTables(connectionMock, List.of("tabularParameters"), CALCULATION_ID);
            verify(databaseServiceMock).saveTabularParameter(connectionMock, tabularParameters, CALCULATION_ID);
            verifyNoMoreInteractions(databaseServiceMock);
        });
    }

    @Test
    void shouldSaveTabularParameterDimensions() {
        final TabularParameters tabularParameters = TabularParameters.builder().name("tabularParameters").build();

        DriverManagerMock.prepare(connectionMock -> {
            objectUnderTest.saveTabularParameterDimensions(CALCULATION_ID, List.of(tabularParameters));

            verify(dimensionTablesServiceMock).save(connectionMock, List.of("tabularParameters_b2531c89"), CALCULATION_ID);
        });
    }

    @Test
    void shouldDeleteAllTables() {
        final Set<String> tableNames = Set.of("table_1", "table_2");

        DriverManagerMock.prepare(connectionMock -> {
            when(dimensionTablesServiceMock.findLostTableNames()).thenReturn(tableNames);

            objectUnderTest.deleteLostTables();

            verify(dimensionTablesServiceMock).findLostTableNames();
            verify(databaseServiceMock).deleteTables(connectionMock, tableNames);
        });
    }

    @Test
    void shouldThrowUncheckedSqlOnDeleteAllTables() {
        final Set<String> tableNames = Set.of("table_1", "table_2");

        DriverManagerMock.prepare(connectionMock -> {
            when(dimensionTablesServiceMock.findLostTableNames()).thenReturn(tableNames);
            doThrow(SQLException.class).when(databaseServiceMock).deleteTables(connectionMock, tableNames);

            Assertions.assertThatThrownBy(() -> objectUnderTest.deleteLostTables())
                    .hasRootCauseExactlyInstanceOf(SQLException.class)
                    .isInstanceOf(UncheckedSqlException.class);

            verify(dimensionTablesServiceMock).findLostTableNames();
            verify(databaseServiceMock).deleteTables(connectionMock, tableNames);
        });
    }
}