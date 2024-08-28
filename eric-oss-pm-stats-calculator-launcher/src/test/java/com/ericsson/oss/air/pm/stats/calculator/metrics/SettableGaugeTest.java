/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.metrics;

import com.codahale.metrics.Gauge;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SettableGaugeTest {
    @Test
    void whenCreatedWithNoInput_thenValueWillBeNull(){
        final Gauge<Object> objectUnderTest = new SettableGauge<>();
        Assertions.assertThat(objectUnderTest.getValue()).isNull();
    }

    @Test
    void whenCreatedWithDefaultValue_thenTypeOfValueWillBeTheSame(){
        final Long defaultValue = 0L;
        final Gauge<Object> objectUnderTest = new SettableGauge<>(defaultValue);

        Assertions.assertThat(objectUnderTest.getValue()).isInstanceOf(Long.class);
    }

    @Test
    void whenValueIsSet_thenValueAndTypeAreUpdated(){
        final Long defaultValue = 0L;
        final SettableGauge<Object> objectUnderTest = new SettableGauge<>(defaultValue);

        final String newValue = "new_value";
        objectUnderTest.setValue(newValue);

        Assertions.assertThat(objectUnderTest.getValue())
                  .isInstanceOf(String.class)
                  .isEqualTo(newValue);
    }
}
