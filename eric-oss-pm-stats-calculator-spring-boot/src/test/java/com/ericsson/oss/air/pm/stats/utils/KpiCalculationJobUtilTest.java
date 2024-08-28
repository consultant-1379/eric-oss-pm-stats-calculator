/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation.KpiCalculationJob;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiCalculationJobUtilTest {

    @MethodSource("provideDoesContainData")
    @ParameterizedTest(name = "[{index}] Provided collection contains ''searched'' execution group: ''{1}''")
    void shouldDecideIfContains(final Collection<KpiCalculationJob> jobs, final boolean expected) {
        final boolean actual = KpiCalculationJobUtil.doesContainExecutionGroup(jobs, "searched");

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> provideDoesContainData() {
        return Stream.of(
                Arguments.of(List.of(), false),
                Arguments.of(List.of(calculationJob()), true)
        );
    }

    static KpiCalculationJob calculationJob() {
        return KpiCalculationJob.builder()
                .withCalculationId(UUID.fromString("ee4de580-1fde-4d40-98d9-375ba4273c6e"))
                .withTimeCreated(Timestamp.valueOf("2023-09-03 12:11:10"))
                .withExecutionGroup("searched")
                .withJobType(KpiType.ON_DEMAND)
                .build();
    }

}