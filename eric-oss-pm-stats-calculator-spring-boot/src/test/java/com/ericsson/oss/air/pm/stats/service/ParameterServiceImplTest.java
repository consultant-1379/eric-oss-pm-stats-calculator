/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.repository.api.ParameterRepository;
import com.ericsson.oss.air.pm.stats.test_utils.DriverManagerMock;

import kpi.model.KpiDefinitionRequest;
import kpi.model.OnDemand;
import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.ParameterType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParameterServiceImplTest {

    @Mock ParameterRepository parameterRepositoryMock;
    @InjectMocks ParameterServiceImpl objectUnderTest;

    @Test
    void shouldSaveParameter_onInsert(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock OnDemand onDemandMock,
                                      @Mock final OnDemandParameter onDemandParameterMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiDefinitionMock.onDemand()).thenReturn(onDemandMock);
            when(onDemandMock.parameters()).thenReturn(List.of(onDemandParameterMock));

            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.insert(kpiDefinitionMock, DEFAULT_COLLECTION_ID));

            verify(kpiDefinitionMock).onDemand();
            verify(onDemandMock).parameters();
            verify(parameterRepositoryMock).saveAllParameters(connectionMock, List.of(onDemandParameterMock), DEFAULT_COLLECTION_ID);
        });
    }

    @Test
    void shouldThrowException_whenCannotSave(@Mock final KpiDefinitionRequest kpiDefinitionMock, @Mock OnDemand onDemandMock,
                                             @Mock final OnDemandParameter onDemandParameterMock) {
        DriverManagerMock.prepare(connectionMock -> {
            when(kpiDefinitionMock.onDemand()).thenReturn(onDemandMock);
            when(onDemandMock.parameters()).thenReturn(List.of(onDemandParameterMock));
            doThrow(new SQLException("Already exists")).when(parameterRepositoryMock)
                .saveAllParameters(connectionMock, List.of(onDemandParameterMock), DEFAULT_COLLECTION_ID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.insert(kpiDefinitionMock, DEFAULT_COLLECTION_ID))
                      .hasRootCauseInstanceOf(SQLException.class)
                      .isInstanceOf(KpiPersistenceException.class)
                      .hasMessage("Parameters could not be saved into the table.");

            verify(kpiDefinitionMock).onDemand();
            verify(onDemandMock).parameters();
            verify(parameterRepositoryMock).saveAllParameters(connectionMock, List.of(onDemandParameterMock), DEFAULT_COLLECTION_ID);
        });
    }

    @Test
    void shouldFindAggregationElementTypeForTabularParameters() {
        final AggregationElement aggregationElement1 = aggregationElement("cell_configuration", "agg_column_0", "agg_column_0");
        final AggregationElement aggregationElement2 = aggregationElement("cell_configuration_2", "agg_column_1", "nodeFDN");

        final List<String> tabularParameterSourceTableNames = List.of("cell_configuration", "cell_configuration_2");

        final List<Parameter> parameters = List.of(parameter("agg_column_0", ParameterType.INTEGER, "cell_configuration"),
                parameter("agg_column_1", ParameterType.STRING, "cell_configuration_2")
        );

        when(parameterRepositoryMock.findParametersForListOfTabularParameter(tabularParameterSourceTableNames)).thenReturn(parameters);

        final Map<String, KpiDataType> actual = objectUnderTest.findAggregationElementTypeForTabularParameter(List.of(aggregationElement1, aggregationElement2));

        final Map<String, KpiDataType> expected = Map.of("agg_column_0", KpiDataType.POSTGRES_INTEGER,
                "nodeFDN", KpiDataType.POSTGRES_STRING);

        verify(parameterRepositoryMock).findParametersForListOfTabularParameter(tabularParameterSourceTableNames);
        Assertions.assertThat(actual).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void shouldThrowExceptionWhenParameterNotFound() {
        final AggregationElement aggregationElement = aggregationElement("cell_configuration", "agg_column_0", "agg_column_0");

        when(parameterRepositoryMock.findParametersForListOfTabularParameter(List.of("cell_configuration"))).thenReturn(Collections.emptyList());

        Assertions.assertThatThrownBy(() -> objectUnderTest.findAggregationElementTypeForTabularParameter(List.of(aggregationElement)))
                .isInstanceOf(KpiPersistenceException.class)
                .hasMessage("Invalid aggregation element. Parameter declaration not found for: 'cell_configuration.agg_column_0'");

        verify(parameterRepositoryMock).findParametersForListOfTabularParameter(List.of("cell_configuration"));
    }

    @Test
    void shouldFindAllSingleParameters() {
        when(parameterRepositoryMock.findAllSingleParameters()).thenReturn(Collections.emptyList());

        objectUnderTest.findAllSingleParameters();

        verify(parameterRepositoryMock).findAllSingleParameters();
    }

    static AggregationElement aggregationElement(final String table, final String source, final String target) {
        return AggregationElement.builder()
                .withExpression(String.format("%s.%s", table, source))
                .withSourceTable(table)
                .withSourceColumn(source)
                .withTargetColumn(target)
                .withIsParametric(false)
                .build();
    }

    static Parameter parameter(final String name, final ParameterType type, final String table) {
        return Parameter.builder()
                .withName(name)
                .withType(type)
                .withTabularParameter(TabularParameter.builder().withName(table).build())
                .build();
    }
}