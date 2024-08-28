/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SparkUtilsTest {

    @Test
    void shouldBuildJobDescription() {
        final String actual = SparkUtils.buildJobDescription("myMetric", "1", "2");

        Assertions.assertThat(actual).isEqualTo("myMetric : 1 : 2");
    }
}