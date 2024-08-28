/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka;

import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TopicPartitionOffsetDistanceTest {

    @Nested
    class DistanceTest {

        @Nested
        class InstantiationPrecondition {
            @Test
            void shouldFail_whenStartOffsetIsGreaterThanEndOffset() {
                Assertions.assertThatThrownBy(() -> OffsetDistance.of(StartOffset.of(20), EndOffset.of(10)))
                          .isInstanceOf(IllegalArgumentException.class)
                          .hasMessage("startOffset is greater than endOffset");
            }
        }

        @Test
        void shouldCalculateDistance() {
            final OffsetDistance offsetDistance = OffsetDistance.of(StartOffset.of(10), EndOffset.of(30));

            final long actual = offsetDistance.distance();

            Assertions.assertThat(actual).isEqualTo(20);
        }


        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        class WithCoveredDistance {

            @Test
            void shouldRaiseException_whenOverFlows() {
                final OffsetDistance offsetDistance = OffsetDistance.of(StartOffset.of(10), EndOffset.of(30));
                Assertions.assertThatThrownBy(() -> offsetDistance.withCoveredDistance(21))
                          .isInstanceOf(IllegalArgumentException.class)
                          .hasMessage("start offset('10') plus covered distance('21') is greater than the end offset('30')");
            }

            @ParameterizedTest
            @MethodSource("provideCoveredWithDistanceData")
            void shouldVerifyCoveredWithDistance(final OffsetDistance offsetDistance, final int coveredDistance) {
                final OffsetDistance actual = offsetDistance.withCoveredDistance(coveredDistance);

                Assertions.assertThat(actual.distance()).isEqualTo(coveredDistance);
                Assertions.assertThat(actual.startOffset().number()).isEqualTo(offsetDistance.startOffset().number());
                Assertions.assertThat(actual.endOffset().number()).isEqualTo(offsetDistance.startOffset().number() + coveredDistance);
            }

            Stream<Arguments> provideCoveredWithDistanceData() {
                return Stream.of(
                        Arguments.of(OffsetDistance.of(StartOffset.of(10), EndOffset.of(30)), 19),
                        Arguments.of(OffsetDistance.of(StartOffset.of(10), EndOffset.of(30)), 20)
                );
            }
        }
    }
}