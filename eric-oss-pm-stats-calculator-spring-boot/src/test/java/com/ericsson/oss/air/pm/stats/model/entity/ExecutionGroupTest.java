/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ExecutionGroupTest {

    @Test
    void shouldReturnNullId() {
        final ExecutionGroup actual = ExecutionGroup.builder().withName("groupName").build();

        Assertions.assertThat(actual.id()).isNull();
        Assertions.assertThat(actual.name()).isEqualTo("groupName");
    }
}
