/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.enumeration;

import static kpi.model.api.enumeration.AggregationType.ARRAY_INDEX_SUM;
import static kpi.model.api.enumeration.AggregationType.ARRAY_INDEX_SUM_DOUBLE;
import static kpi.model.api.enumeration.AggregationType.ARRAY_INDEX_SUM_INTEGER;
import static kpi.model.api.enumeration.AggregationType.ARRAY_INDEX_SUM_LONG;
import static kpi.model.api.enumeration.AggregationType.FIRST;
import static kpi.model.api.enumeration.AggregationType.MAX;
import static kpi.model.api.enumeration.AggregationType.PERCENTILE_INDEX_80;
import static kpi.model.api.enumeration.AggregationType.PERCENTILE_INDEX_90;
import static kpi.model.api.enumeration.AggregationType.SUM;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AggregationTypeTest {
    @Test
    void shouldValidateValues() {
        Assertions.assertThat(AggregationType.values()).containsExactlyInAnyOrder(
                SUM,
                MAX,
                ARRAY_INDEX_SUM,
                ARRAY_INDEX_SUM_INTEGER,
                ARRAY_INDEX_SUM_DOUBLE,
                ARRAY_INDEX_SUM_LONG,
                PERCENTILE_INDEX_80,
                PERCENTILE_INDEX_90,
                FIRST
        );
    }
}