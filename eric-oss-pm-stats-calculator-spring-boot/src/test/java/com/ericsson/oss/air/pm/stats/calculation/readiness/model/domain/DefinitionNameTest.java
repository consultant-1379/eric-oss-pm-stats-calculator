/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DefinitionNameTest {

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class HasSameName {

        @MethodSource("provideHasSameNameData")
        @ParameterizedTest(name = "[{index}] ''{0}'' is same ''{1}'' ==> ''{2}''")
        void verifyHasSameName(final String definitionName, final String kpiName, final boolean expected) {
            final boolean actual = DefinitionName.of(definitionName).hasSameName(KpiDefinitionEntity.builder().withName(kpiName).build());

            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideHasSameNameData() {
            return Stream.of(
                    Arguments.of("name", "name", true),
                    Arguments.of("name", "Name", false),
                    Arguments.of("name", null, false)
            );
        }
    }
}