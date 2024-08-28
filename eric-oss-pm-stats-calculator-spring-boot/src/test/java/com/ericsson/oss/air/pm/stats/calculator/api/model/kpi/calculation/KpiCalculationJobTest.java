/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.calculation;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.stream.Stream;

class KpiCalculationJobTest {
    private static final Timestamp TEST_TIME = Timestamp.valueOf(LocalDateTime.of(LocalDate.of(2_022, Month.APRIL, 12), LocalTime.NOON));

    private static final String FIELD_TIME_CREATED = "timeCreated";

    @Test
    void shouldVerifyDefaultsBuilder() {
        final KpiCalculationJob kpiCalculationJob = KpiCalculationJob.defaultsBuilder().build();

        Assertions.assertThat(kpiCalculationJob.getCalculationId()).isNotNull();
        Assertions.assertThat(kpiCalculationJob.getTimeCreated()).isNotNull();
    }

    @Test
    void shouldCreateInstance_withNonNullTimestamp() {
        final KpiCalculationJob kpiCalculationJob = KpiCalculationJob.builder().withTimeCreated(TEST_TIME).build();

        Assertions.assertThat(kpiCalculationJob)
                .extracting(FIELD_TIME_CREATED, InstanceOfAssertFactories.type(Timestamp.class))
                .isNotSameAs(TEST_TIME)
                .isEqualTo(TEST_TIME);
    }

    @Test
    void shouldReturnNewTimestamp_whenGettingTimeCreated_andTimeCreatedIsNotNull() throws IllegalAccessException {
        final KpiCalculationJob kpiCalculationJob = KpiCalculationJob.builder().withTimeCreated(TEST_TIME).build();

        FieldUtils.writeDeclaredField(kpiCalculationJob, FIELD_TIME_CREATED, TEST_TIME, true);

        Assertions.assertThat(kpiCalculationJob.getTimeCreated())
                .isNotSameAs(TEST_TIME)
                .isEqualTo(TEST_TIME);
    }

    @MethodSource("provideIsComplexData")
    @ParameterizedTest(name = "[{index}] KpiCalculationJob is complex ==> ''{1}''")
    void isComplex(final KpiCalculationJob kpiCalculationJob, final boolean expected) {
        Assertions.assertThat(kpiCalculationJob.isComplex()).isEqualTo(expected);
    }

    @MethodSource("provideIsSimpleData")
    @ParameterizedTest(name = "[{index}] KpiCalculationJob is simple ==> ''{1}''")
    void isSimple(final KpiCalculationJob kpiCalculationJob, final boolean expected) {
        Assertions.assertThat(kpiCalculationJob.isSimple()).isEqualTo(expected);
    }

    @MethodSource("provideIsOnDemandData")
    @ParameterizedTest(name = "[{index}] KpiCalculationJob with execution group ''{0}'' is ON_DEMAND ==> ''{1}''")
    void shouldVerifyIsOnDemand(final String executionGroup, final boolean expected) {
        final KpiCalculationJob kpiCalculationJob = KpiCalculationJob.builder().withExecutionGroup(executionGroup).withTimeCreated(TEST_TIME).build();

        final boolean actual = kpiCalculationJob.isOnDemand();

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> provideIsOnDemandData() {
        return Stream.of(
                Arguments.of("ON_DEMAND", true),
                Arguments.of("on_demand", true),
                Arguments.of("COMPLEX", false),
                Arguments.of("big_cell_sector", false),
                Arguments.of(null, false)
        );
    }

    private static Stream<Arguments> provideIsComplexData() {
        final KpiCalculationJob simpleJob = KpiCalculationJob.builder().withJobType(KpiType.SCHEDULED_SIMPLE).withTimeCreated(TEST_TIME).build();
        final KpiCalculationJob onDemandJob = KpiCalculationJob.builder().withJobType(KpiType.ON_DEMAND).withTimeCreated(TEST_TIME).build();
        final KpiCalculationJob complexJob = KpiCalculationJob.builder().withJobType(KpiType.SCHEDULED_COMPLEX).withTimeCreated(TEST_TIME).build();
        return Stream.of(
                Arguments.of(simpleJob, false),
                Arguments.of(onDemandJob, false),
                Arguments.of(complexJob, true)
        );
    }

    static Stream<Arguments> provideIsSimpleData() {
        return Stream.of(
                Arguments.of(kpiDefinition(KpiType.SCHEDULED_SIMPLE), true),
                Arguments.of(kpiDefinition(KpiType.SCHEDULED_COMPLEX), false),
                Arguments.of(kpiDefinition(KpiType.ON_DEMAND), false)
        );
    }

    private static KpiCalculationJob kpiDefinition(final KpiType type) {
        return KpiCalculationJob.builder().withJobType(type).withTimeCreated(TEST_TIME).build();
    }

}