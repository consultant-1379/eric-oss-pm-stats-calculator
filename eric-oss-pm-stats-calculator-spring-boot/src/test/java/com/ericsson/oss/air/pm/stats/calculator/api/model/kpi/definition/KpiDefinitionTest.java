/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Unit tests for {@link KpiDefinition}.
 */
class KpiDefinitionTest {

    @Test
    void datasourceShouldConstructedCorrectly() {
        KpiDefinition objectUnderTest = KpiDefinition.builder()
                .withInpDataIdentifier(DataIdentifier.of("data_identifier"))
                .build();

        assertSoftly(softly -> {
            softly.assertThat(objectUnderTest.datasource()).isEqualTo("data_identifier");
        });
    }
}