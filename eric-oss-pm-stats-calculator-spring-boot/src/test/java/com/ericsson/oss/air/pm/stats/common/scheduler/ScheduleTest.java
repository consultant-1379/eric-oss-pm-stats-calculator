/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Schedule}.
 */
class ScheduleTest {

    @Test
    void whenCreatingSchedule_andRetrievingContext_thenContextIsImmutable() {
        final Schedule input = new Schedule("name", Collections.singletonMap("key", "value"));
        final Map<String, Object> result = input.getContext();

        assertThat(result).containsExactly(entry("key", "value"));

        Assertions.assertThatThrownBy(() -> result.put("new", "value")).isInstanceOf(UnsupportedOperationException.class);
    }
}
