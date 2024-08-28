/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test;

import static com.ericsson.oss.air.pm.stats.common.resources.utils.ResourceLoaderUtils.getClasspathResourceAsString;
import static com.ericsson.oss.air.pm.stats.test.util.RequestUtils.onFailure;

import java.util.List;

import com.ericsson.oss.air.pm.stats.test.dto.input._assert.ErrorResponseAssertions;
import com.ericsson.oss.air.pm.stats.test.integration.IntegrationTest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
@IntegrationTest
class SqlDatasourceIntTests {

    @Test
    void shouldFailOnDefinitionsPointingOutsideOfExecutionGroupInCaseOfInMemoryDatasource() throws Exception {
        final String payload = getClasspathResourceAsString("datasource/complex_in_memory_outside_of_execution_group.json");
        onFailure().postKpiDefinition(payload,errorResponse -> {
            ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessages(List.of(
                    "KPI Definition 'level_1' with reference " +
                    "'RESOLVED kpi_inmemory.in_memory_outside_scope in_memory_outside_scope.base_kpi_definition[EXPRESSION]' " +
                    "points outside the execution group of 'outside_execution_group' but uses in-memory datasource"
            ));
        });
    }

    @Test
    void shouldFailOnUnknownDatasource() throws Exception {
        final String payload = getClasspathResourceAsString("datasource/unknown_datasource.json");
        onFailure().postKpiDefinition(payload,errorResponse -> {
            ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessage(
                    "Datasource 'random_datasource' is unknown. Use sources '[kpi_db, kpi_inmemory, kpi_post_agg, tabular_parameters]'"
            );
        });
    }
}
