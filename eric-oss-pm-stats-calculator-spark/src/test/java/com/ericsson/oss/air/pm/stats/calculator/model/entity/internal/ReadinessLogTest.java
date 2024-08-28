/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.model.entity.internal;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

class ReadinessLogTest {
    @Nested
    class Datasource {
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        class SameDatasource {
            @MethodSource("provideHasSameDatasourceData")
            @ParameterizedTest(name = "[{index}] Left ''{0}'' has same datasource as right ''{1}'' ==> ''{2}'')")
            void shouldVerifyHasSameDatasource(final ReadinessLog left, final ReadinessLog right, final boolean expected) {
                final boolean actual = left.hasSameDatasource(right);
                Assertions.assertThat(actual).isEqualTo(expected);
            }

            Stream<Arguments> provideHasSameDatasourceData() {
                return Stream.of(
                        Arguments.of(readinessLog("datasource"), readinessLog("datasource"), true),
                        Arguments.of(readinessLog(null), readinessLog("datasource"), false),
                        Arguments.of(readinessLog("DataSource"), readinessLog("datasource"), false),
                        Arguments.of(readinessLog("datasource"), readinessLog("DataSource"), false)
                );
            }

            ReadinessLog readinessLog(final String datasource) {
                return ReadinessLog.builder().datasource(datasource).build();
            }
        }

        @Nested
        @ExtendWith(MockitoExtension.class)
        class DifferentDatasource {
            @Spy ReadinessLog objectUnderTest;

            @Test
            void shouldVerifyHasSameDatasource(@Mock final ReadinessLog otherMock) {
                doReturn(true).when(objectUnderTest).hasSameDatasource(otherMock);

                final boolean actual = objectUnderTest.hasDifferentDatasource(otherMock);

                verify(objectUnderTest).hasSameDatasource(otherMock);

                Assertions.assertThat(actual).isFalse();
            }
        }
    }

    @Nested
    class CalculationId {
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        class SameCalculationId {
            @MethodSource("provideHasSameCalculationIdData")
            @ParameterizedTest(name = "[{index}] Left ''{0}'' has same calculation id as right ''{1}'' ==> ''{2}'')")
            void shouldVerifyHasSameCalculationId(final ReadinessLog left, final ReadinessLog right, final boolean expected) {
                final boolean actual = left.hasSameCalculationId(right);
                Assertions.assertThat(actual).isEqualTo(expected);
            }

            Stream<Arguments> provideHasSameCalculationIdData() {
                return Stream.of(
                        Arguments.of(
                                readinessLog(uuid("0a6b0737-5c78-407c-86aa-2fff4bbdbb88")),
                                readinessLog(uuid("0a6b0737-5c78-407c-86aa-2fff4bbdbb88")),
                                true
                        ),
                        Arguments.of(
                                readinessLog(uuid("0a6b0737-5c78-407c-86aa-2fff4bbdbb88")),
                                readinessLog(uuid("ebfe2392-fe03-428b-a458-87878863fae1")),
                                false
                        )
                );
            }

            UUID uuid(final String uuid) {
                return UUID.fromString(uuid);
            }

            ReadinessLog readinessLog(final UUID calculationId) {
                final Calculation calculation = Calculation.builder().id(calculationId).build();
                return ReadinessLog.builder().kpiCalculationId(calculation).build();
            }
        }

        @Nested
        @ExtendWith(MockitoExtension.class)
        class DifferentCalculationId {
            @Spy ReadinessLog objectUnderTest;

            @Test
            void shouldVerifyHasDifferentCalculationId(@Mock final ReadinessLog otherMock) {
                doReturn(true).when(objectUnderTest).hasSameCalculationId(otherMock);

                final boolean actual = objectUnderTest.hasDifferentCalculationId(otherMock);

                verify(objectUnderTest).hasSameCalculationId(otherMock);

                Assertions.assertThat(actual).isFalse();
            }
        }
    }
}