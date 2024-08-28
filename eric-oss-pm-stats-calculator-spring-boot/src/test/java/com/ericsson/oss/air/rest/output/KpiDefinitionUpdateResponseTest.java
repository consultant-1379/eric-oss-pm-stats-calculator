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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class KpiDefinitionUpdateResponseTest {
    static final String KPI_DEFINITION_UPDATE_RESPONSE = "json/kpi_definition_update_response.json";
    ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Test
    void shouldSerialize() {
        final KpiDefinitionUpdateResponse kpiDefinitionUpdateResponse = KpiDefinitionUpdateResponse.builder()
                .name("simple_one")
                .alias("alias_simple_table_one")
                .aggregationPeriod(60)
                .expression("EXPRESSION")
                .objectType("INTEGER")
                .aggregationType("SUM")
                .aggregationElements(List.of("table.column1", "table.column2"))
                .exportable(true)
                .filters(List.of("f1","f2"))
                .reexportLateData(true)
                .dataReliabilityOffset(1)
                .dataLookbackLimit(1)
                .inpDataIdentifier("dataSpace|category|name")
                .build();

        final String actual = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(kpiDefinitionUpdateResponse);
        final String expected = ResourceLoaderUtils.getClasspathResourceAsString(KPI_DEFINITION_UPDATE_RESPONSE);

        assertThat(actual).isEqualToIgnoringNewLines(expected);
    }
}
