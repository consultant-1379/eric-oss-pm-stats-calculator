/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter.model;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiTest {

    @MethodSource("provideExportableAndReexportValue")
    @ParameterizedTest(name = "[{index}] exportable: ''{0}'', reexport: ''{1}''")
    void shouldKpiDefinitionToKpiMappingBeCorrect(final boolean exportable, final boolean reexport) {
        final Kpi actual = Kpi.of("KPI", 1660349587, 1663854178, reexport, exportable);

        Assertions.assertThat(actual.getName()).isEqualTo("KPI");
        Assertions.assertThat(actual.isExportable()).isEqualTo(exportable);
        Assertions.assertThat(actual.isReexportLateData()).isEqualTo(reexport);
        Assertions.assertThat(actual.getReliabilityThreshold()).isEqualTo(1660349587);
        Assertions.assertThat(actual.getCalculationStartTime()).isEqualTo(1663854178);
    }

    static Stream<Arguments> provideExportableAndReexportValue() {
        return Stream.of(
                Arguments.of(true, true),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(false, false)
        );
    }
}
