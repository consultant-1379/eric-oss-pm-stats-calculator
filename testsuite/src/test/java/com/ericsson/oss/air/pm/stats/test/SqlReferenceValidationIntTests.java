/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Slf4j
@IntegrationTest
class SqlReferenceValidationIntTests {

    @Test
    @Order(10)
    void shouldFailOnUnresolvedReferences() throws Exception {
        final String payload = getClasspathResourceAsString("sql_reference/invalid_sql_reference.json");
        onFailure().postKpiDefinition(payload,errorResponse -> {
            ErrorResponseAssertions.assertThat(errorResponse).isBadRequestWithMessagesInAnyOrder(List.of(
                    "KPI Definition 'dim_enrich_' has reference 'UNRESOLVED <relation> cell_configuration_test.invalid_aggregation AS agg_column_0[AGGREGATION_ELEMENTS]'",
                    "KPI Definition 'invalid_agg_element_test_' has reference 'UNRESOLVED <relation> kpi_simple_60.invalid_column[EXPRESSION]'",
                    "KPI Definition 'invalid_agg_element_test_' has reference 'UNRESOLVED <relation> kpi_simple_60.invalid_agg_element[AGGREGATION_ELEMENTS]'",
                    "KPI Definition 'invalid_agg_element_test_' has reference 'UNRESOLVED <relation> kpi_db.kpi_sector_60.aggregation_begin_time[FILTERS]'"
            ));
        });
    }
}
