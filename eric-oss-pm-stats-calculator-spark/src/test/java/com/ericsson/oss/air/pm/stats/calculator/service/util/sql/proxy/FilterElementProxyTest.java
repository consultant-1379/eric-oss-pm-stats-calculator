/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.sql.proxy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FilterElementProxyTest {

    @Test
    void shouldVerifyProxy() {
        final String element = "kpi_db://kpi_cell_guid_1440.TO_DATE(aggregation_begin_time) = '${param.date_for_filter}'";
        final FilterElementProxy actual = new FilterElementProxy(element);

        Assertions.assertThat(actual.value()).isEqualTo("kpi_db://kpi_cell_guid_1440.TO_DATE(aggregation_begin_time) = '${param.date_for_filter}'");
        Assertions.assertThat(actual.name()).isEqualTo("filter-element-proxy");
    }
}