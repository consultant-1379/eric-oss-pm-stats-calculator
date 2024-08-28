/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.configuration.environment.model;

import java.util.NoSuchElementException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EnvironmentValueTest {
    @Test
    void shouldCreateOfNullable_nonNull() {
        final EnvironmentValue<Integer> actual = EnvironmentValue.ofNullable(5);
        Assertions.assertThat(actual.isPresent()).isTrue();
        Assertions.assertThat(actual.isMissing()).isFalse();
        Assertions.assertThat(actual.value()).isEqualTo(5);
    }

    @Test
    void shouldCreateOfNullable_null() {
        final EnvironmentValue<Integer> actual = EnvironmentValue.ofNullable(null);
        Assertions.assertThat(actual.isPresent()).isFalse();
        Assertions.assertThat(actual.isMissing()).isTrue();
        Assertions.assertThatThrownBy(actual::value).isInstanceOf(NoSuchElementException.class).hasMessage("value is not present");
    }
}