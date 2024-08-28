/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import java.util.Queue;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class KpiCalculatorJmxPortPoolTest {
    @Test
    void shouldCreateJmxPortMapping_withDefaultValues() {
        final Queue<String> actual = KpiCalculatorJmxPortPool.createJmxPortMapping(10010, 2);

        Assertions.assertThat(actual).containsExactly("10010", "10011");
    }
}