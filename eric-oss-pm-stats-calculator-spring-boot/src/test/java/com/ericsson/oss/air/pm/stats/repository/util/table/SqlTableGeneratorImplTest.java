/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter.ParameterBuilder;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlAppender;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;

import kpi.model.ondemand.ParameterType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class SqlTableGeneratorImplTest {
    private static final String APPEND_AGGREGATION_ELEMENT_COLUMNS = "appendAggregationElementColumns";
    private static final String APPEND_KPI_DEFINITION_COLUMN = "appendKpiDefinitionColumn";
    private static final String APPEND_PRIMARY_KEY = "appendPrimaryKey";
    private static final String APPEND_PARTITION = "appendPartition";
    private static final String APPEND_TIMESTAMP_COLUMNS = "appendTimestampColumns";

    @Mock
    SqlAppender sqlAppenderMock;
    @InjectMocks
    SqlTableGeneratorImpl objectUnderTest;

    @Test
    void shouldGenerateDefaultOutputTable() {
        final KpiDefinitionEntity definition = createDefinition("definition1", Arrays.asList("agg1", "agg2"));
        final List<KpiDefinitionEntity> definitions = List.of(definition);

        doAnswer(append(String.format("%s)", APPEND_AGGREGATION_ELEMENT_COLUMNS)))
                .when(sqlAppenderMock).appendAggregationElementColumns(any(StringBuilder.class), anySet(), any());
        doAnswer(append(String.format(" (%s)", APPEND_KPI_DEFINITION_COLUMN)))
                .when(sqlAppenderMock).appendColumnNameAndType(any(StringBuilder.class), any(), any());
        doAnswer(append(String.format(" (%s", APPEND_PRIMARY_KEY)))
                .when(sqlAppenderMock).appendPrimaryKey(any(StringBuilder.class), anySet());

        final String actual = objectUnderTest.generateOutputTable(TableCreationInformation.of("tableName", "-1", definitions), new HashMap<>());

        verify(sqlAppenderMock).appendAggregationElementColumns(any(StringBuilder.class), anySet(), any());
        verify(sqlAppenderMock).appendColumnNameAndType(any(StringBuilder.class), any(), any());
        verify(sqlAppenderMock).appendPrimaryKey(any(StringBuilder.class), anySet());
        verifyNoMoreInteractions(sqlAppenderMock);

        Assertions.assertThat(actual)
                .isEqualTo("CREATE TABLE IF NOT EXISTS kpi_service_db.kpi.tableName (%s) (%s) (%s);",
                        APPEND_AGGREGATION_ELEMENT_COLUMNS,
                        APPEND_KPI_DEFINITION_COLUMN,
                        APPEND_PRIMARY_KEY);
    }

    @Test
    void shouldGenerateNonDefaultOutputTable() {
        final KpiDefinitionEntity definition = createDefinition("definition1", Arrays.asList("agg1", "agg2"));
        final List<KpiDefinitionEntity> definitions = Collections.singletonList(definition);

        doAnswer(append(String.format("%s)", APPEND_AGGREGATION_ELEMENT_COLUMNS)))
                .when(sqlAppenderMock).appendAggregationElementColumns(any(StringBuilder.class), anySet(), any());
        doAnswer(append(String.format(" (%s)", APPEND_KPI_DEFINITION_COLUMN)))
                .when(sqlAppenderMock).appendColumnNameAndType(any(StringBuilder.class), any(), any());
        doAnswer(append(String.format(" (%s ", APPEND_TIMESTAMP_COLUMNS)))
                .when(sqlAppenderMock).appendTimestampColumns(any(StringBuilder.class));
        doAnswer(append(String.format(" (%s)", APPEND_PARTITION)))
                .when(sqlAppenderMock).appendPartition(any(StringBuilder.class));

        final String actual = objectUnderTest.generateOutputTable(TableCreationInformation.of("tableName", "60", definitions), new HashMap<>());

        verify(sqlAppenderMock).appendAggregationElementColumns(any(StringBuilder.class), anySet(), any());
        verify(sqlAppenderMock).appendColumnNameAndType(any(StringBuilder.class), any(), any());
        verify(sqlAppenderMock).appendTimestampColumns(any(StringBuilder.class));
        verify(sqlAppenderMock).appendPartition(any(StringBuilder.class));
        verifyNoMoreInteractions(sqlAppenderMock);

        Assertions.assertThat(actual)
                .isEqualTo("CREATE TABLE IF NOT EXISTS kpi_service_db.kpi.tableName (%s) (%s) (%s ) (%s)",
                        APPEND_AGGREGATION_ELEMENT_COLUMNS,
                        APPEND_KPI_DEFINITION_COLUMN,
                        APPEND_TIMESTAMP_COLUMNS,
                        APPEND_PARTITION);
    }

    @Test
    void shouldNotAddKpiDefinitionName_whenItIsContainedByTheAggregationElements() {
        final KpiDefinitionEntity definition = createDefinition("definition1", Arrays.asList("agg1", "agg2", "definition1"));
        final List<KpiDefinitionEntity> definitions = Collections.singletonList(definition);

        doAnswer(append(String.format("%s)", APPEND_AGGREGATION_ELEMENT_COLUMNS)))
                .when(sqlAppenderMock).appendAggregationElementColumns(any(StringBuilder.class), anySet(), any());
        doAnswer(append(String.format(" (%s", APPEND_PRIMARY_KEY)))
                .when(sqlAppenderMock).appendPrimaryKey(any(StringBuilder.class), anySet());

        final String actual = objectUnderTest.generateOutputTable(TableCreationInformation.of("tableName", "-1", definitions), new HashMap<>());

        verify(sqlAppenderMock).appendAggregationElementColumns(any(StringBuilder.class), anySet(), any());
        verify(sqlAppenderMock).appendPrimaryKey(any(StringBuilder.class), anySet());
        verifyNoMoreInteractions(sqlAppenderMock);

        Assertions.assertThat(actual)
                .isEqualTo("CREATE TABLE IF NOT EXISTS kpi_service_db.kpi.tableName (%s) (%s);",
                        APPEND_AGGREGATION_ELEMENT_COLUMNS,
                        APPEND_PRIMARY_KEY);
    }

    @Test
    void shouldGenerateTabularParameterTableSql() {
        final Parameter parameter1 = parameter("cell_config", ParameterType.INTEGER);
        final Parameter parameter2 = parameter("cell_config2", ParameterType.STRING);
        final List<Parameter> parameters = List.of(parameter1, parameter2);

        doAnswer(append("cell_config int4, "))
                .when(sqlAppenderMock).appendColumnNameAndType(any(StringBuilder.class), eq("cell_config"), eq("INTEGER"));
        doAnswer(append("cell_config2 varchar(255), "))
                .when(sqlAppenderMock).appendColumnNameAndType(any(StringBuilder.class), eq("cell_config2"), eq("STRING"));

        final String actual = objectUnderTest.generateTabularParameterTableSql(parameters, "tabular_parameter_test");

        verify(sqlAppenderMock).appendColumnNameAndType(any(StringBuilder.class), eq("cell_config"), eq("INTEGER"));
        verify(sqlAppenderMock).appendColumnNameAndType(any(StringBuilder.class), eq("cell_config2"), eq("STRING"));

        Assertions.assertThat(actual)
                .isEqualTo("CREATE TABLE kpi_service_db.kpi.tabular_parameter_test (cell_config int4, cell_config2 varchar(255) )");
    }

    private static Answer<Void> append(final String stubText) {
        return invocation -> {
            final StringBuilder stringBuilder = invocation.getArgument(0);
            stringBuilder.append(stubText);

            return null;
        };
    }

    static KpiDefinitionEntity createDefinition(final String name, final List<String> aggregationElements) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withAggregationElements(aggregationElements);
        return builder.build();
    }

    static Parameter parameter(final String name, final ParameterType type) {
        final ParameterBuilder builder = Parameter.builder();
        builder.withName(name);
        builder.withType(type);
        return builder.build();
    }
}