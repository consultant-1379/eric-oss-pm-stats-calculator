/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi;

import com.ericsson.oss.air.pm.stats.calculator.api.model.SqlDataType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class KpiAttributeTest {
    @Test
    void shouldCreateKpiAttribute() {
        final String name = "name";

        final KpiAttribute actual = new KpiAttribute();
        actual.setName(name);
        actual.setType(SqlDataType.BOOLEAN);
        actual.setOptional(true);
        actual.setParameterizable(true);

        Assertions.assertThat(actual.getName()).isEqualTo(name);
        Assertions.assertThat(actual.getType()).isEqualTo(SqlDataType.BOOLEAN);
        Assertions.assertThat(actual.isOptional()).isTrue();
        Assertions.assertThat(actual.isParameterizable()).isTrue();
    }
}