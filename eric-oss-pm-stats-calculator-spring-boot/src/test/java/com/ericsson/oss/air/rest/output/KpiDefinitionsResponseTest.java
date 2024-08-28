/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.output;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandParameterDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandParameterDto.OnDemandParameterDtoBuilder;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandTableListDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandTabularParameterDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandTabularParameterDto.OnDemandTabularParameterDtoBuilder;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.TableDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.TableListDto;
import com.ericsson.oss.air.rest.test_utils.KpiDefinitionsResponseObjectBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class KpiDefinitionsResponseTest {
    static final String KPI_DEFINITIONS_RESPONSE = "json/kpi_definitions_response.json";
    ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Test
    void shouldSerialize() {
        final TableDto simpleTable1 = KpiDefinitionsResponseObjectBuilder.tableDto(
                "alias_simple_table_1",
                List.of(
                        KpiDefinitionsResponseObjectBuilder.simpleKpiDefinitionDto("simple_one"),
                        KpiDefinitionsResponseObjectBuilder.simpleKpiDefinitionDto("simple_two")));

        final TableDto simpleTable2 = KpiDefinitionsResponseObjectBuilder.tableDto(
                "alias_simple_table_2",
                List.of(
                        KpiDefinitionsResponseObjectBuilder.simpleKpiDefinitionDto("simple_three")));

        final TableDto complexTable1 = KpiDefinitionsResponseObjectBuilder.tableDto(
                "alias_complex_table_1",
                List.of(
                        KpiDefinitionsResponseObjectBuilder.complexKpiDefinitionDto("complex_one"),
                        KpiDefinitionsResponseObjectBuilder.complexKpiDefinitionDto("complex_two")));

        final TableDto onDemandTable1 = KpiDefinitionsResponseObjectBuilder.tableDto(
                "alias_on_demand_table_1",
                List.of(
                        KpiDefinitionsResponseObjectBuilder.onDemandKpiDefinitionDto("on_demand_one"),
                        KpiDefinitionsResponseObjectBuilder.onDemandKpiDefinitionDto("on_demand_two")));

        final List<OnDemandTabularParameterDto> onDemandTabularParameters = List.of(tabularParameter("cell_configuration", List.of(
                column("target_throughput_r", "FLOAT"),
                column("min_rops_for_reliability", "Integer")
        )));
        final List<OnDemandParameterDto> onDemandParameters = List.of(OnDemandParameterDto.builder().name("execution_id").type("STRING").build());

        final TableListDto simpleTables = new TableListDto(List.of(simpleTable1, simpleTable2));
        final TableListDto complexTable = new TableListDto(List.of(complexTable1));
        final OnDemandTableListDto onDemandTable = new OnDemandTableListDto(onDemandParameters, onDemandTabularParameters, List.of(onDemandTable1));

        final KpiDefinitionsResponse kpiDefinitionsResponse = new KpiDefinitionsResponse(simpleTables, complexTable, onDemandTable);

        final String kpiDefinitionsResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(kpiDefinitionsResponse);
        final String expected = ResourceLoaderUtils.getClasspathResourceAsString(KPI_DEFINITIONS_RESPONSE);

        assertThat(kpiDefinitionsResponseJson).isEqualToIgnoringNewLines(expected);
    }

    private static OnDemandTabularParameterDto tabularParameter(final String cellConfiguration, final List<OnDemandParameterDto> columns) {
        final OnDemandTabularParameterDtoBuilder builder = OnDemandTabularParameterDto.builder();
        builder.name(cellConfiguration);
        builder.columns(columns);
        return builder.build();
    }

    private static OnDemandParameterDto column(final String name, final String type) {
        final OnDemandParameterDtoBuilder builder = OnDemandParameterDto.builder();
        builder.name(name);
        builder.type(type);
        return builder.build();
    }
}
