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

import com.ericsson.oss.air.pm.stats.calculator.api.model.SchemaAttribute;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class KpiTest {
    @Test
    void shouldReturnUnmodifiableAttributes() {
        final ArrayList<KpiAttribute> attributes = new ArrayList<>(5);
        final Kpi kpi = new Kpi(attributes);

        final List<SchemaAttribute> actual = kpi.getAttributes();

        Assertions.assertThat(actual).isUnmodifiable();
    }
}