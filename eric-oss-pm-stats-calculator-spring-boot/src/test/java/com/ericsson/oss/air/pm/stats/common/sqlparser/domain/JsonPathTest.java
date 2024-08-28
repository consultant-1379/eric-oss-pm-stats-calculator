/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.domain;

import java.util.Collection;
import java.util.List;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath.Part;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonPathTest {
    @Test
    void shouldGetUnmodifiableParts() {
        final JsonPath jsonPath = JsonPath.of(List.of("schemaName", "recordName", "kpiName"));
        final Collection<Part> actual = jsonPath.parts();
        Assertions.assertThat(actual).isUnmodifiable().containsExactly(Part.of("schemaName"), Part.of("recordName"), Part.of("kpiName"));
    }

    @Test
    void shouldVerifyToString() {
        final JsonPath actual = JsonPath.of(List.of("schemaName", "recordName", "kpiName"));
        Assertions.assertThat(actual).hasToString("schemaName.recordName.kpiName");
    }
}