/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.bucket.distribute;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Partition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.TopicPartitionOffsetDistance.OffsetDistance;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.EndOffset;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset.StartOffset;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@Slf4j
@ExtendWith(MockitoExtension.class)
class EvenBucketDistributorTest {
    @Mock CalculationProperties calculationPropertiesMock;

    @InjectMocks EvenBucketDistributor objectUnderTest;

    @Test
    void shouldRaiseException_whenRemainingBucketSizeIsGreaterThanRemainingDistance() {
        when(calculationPropertiesMock.getKafkaBucketSize()).thenReturn(101);

        Assertions.assertThatThrownBy(() -> objectUnderTest.distribute(ImmutableMap.of(partition(0), offsetDistance(0, 100))))
                  .isInstanceOf(IllegalArgumentException.class)
                  .hasMessage("remainingBucketSize<'101'> is greater than remaining distance<'100'>");

        verify(calculationPropertiesMock).getKafkaBucketSize();
    }

    @ParameterizedTest
    @MethodSource("provideDistributeEvenlyData")
    void shouldDistributeEvenly(final int bucketSize,
                                final Map<Partition, OffsetDistance> partitionOffsetDistance,
                                final Map<Partition, OffsetDistance> expected) {
        final StringBuilder descriptionReportBuilder = new StringBuilder(String.format("Assertions:%n"));
        Assertions.setDescriptionConsumer(description -> descriptionReportBuilder.append(String.format("-- %s%n", description)));

        when(calculationPropertiesMock.getKafkaBucketSize()).thenReturn(bucketSize);

        final Map<Partition, OffsetDistance> actual = objectUnderTest.distribute(partitionOffsetDistance);

        verify(calculationPropertiesMock).getKafkaBucketSize();

        final long maxDistance = distance(partitionOffsetDistance.values());
        final long coveredDistance = distance(actual.values());

        if (bucketSize <= maxDistance) {
            Assertions.assertThat(coveredDistance).as("Must read all messages if possible").isEqualTo(bucketSize);
        }

        Assertions.assertThat(coveredDistance).as(
                "Max '%s' messages, bucket size '%s' covered messages '%s' ratio '%.2f'",
                maxDistance,
                bucketSize,
                coveredDistance,
                coveredDistance / (double) bucketSize
        ).isLessThanOrEqualTo(bucketSize);

        Assertions.assertThat(actual)
                  .as(buildRatioReport(partitionOffsetDistance, actual))
                  .containsExactlyInAnyOrderEntriesOf(expected);

        System.out.println(descriptionReportBuilder);
    }

    static Stream<Arguments> provideDistributeEvenlyData() {
        return Stream.of(
                Arguments.of(
                        bucketSize(30),
                        ImmutableMap.of(partition(0), offsetDistance(50, 80)),
                        ImmutableMap.of(partition(0), offsetDistance(50, 80))
                ),
                Arguments.of(
                        bucketSize(30),
                        ImmutableMap.of(
                                partition(0), offsetDistance(10, 20),
                                partition(1), offsetDistance(30, 35),
                                partition(2), offsetDistance(30, 45)
                        ),
                        ImmutableMap.of(
                                partition(0), offsetDistance(10, 20),
                                partition(1), offsetDistance(30, 35),
                                partition(2), offsetDistance(30, 45)
                        )
                ),
                Arguments.of(
                        bucketSize(10),
                        ImmutableMap.of(
                                partition(0), offsetDistance(0, 5),
                                partition(1), offsetDistance(3, 5),
                                partition(2), offsetDistance(6, 16)
                        ),
                        ImmutableMap.of(
                                partition(0), offsetDistance(0, 3),
                                partition(1), offsetDistance(3, 4),
                                partition(2), offsetDistance(6, 12)
                        )
                ),
                Arguments.of(
                        bucketSize(3),
                        ImmutableMap.of(
                                partition(0), offsetDistance(0, 3),
                                partition(1), offsetDistance(3, 6),
                                partition(2), offsetDistance(6, 9)
                        ),
                        ImmutableMap.of(
                                partition(0), offsetDistance(0, 1),
                                partition(1), offsetDistance(3, 4),
                                partition(2), offsetDistance(6, 7)
                        )
                ),
                Arguments.of(
                        bucketSize(50),
                        ImmutableMap.of(
                                partition(0), offsetDistance(0, 20),
                                partition(1), offsetDistance(0, 10),
                                partition(2), offsetDistance(0, 5),
                                partition(3), offsetDistance(0, 30),
                                partition(4), offsetDistance(0, 0)
                        ),
                        ImmutableMap.of(
                                partition(0), offsetDistance(0, 15),
                                partition(1), offsetDistance(0, 8),
                                partition(2), offsetDistance(0, 4),
                                partition(3), offsetDistance(0, 23),
                                partition(4), offsetDistance(0, 0)
                        )
                ),
                Arguments.of(
                        bucketSize(25),
                        ImmutableMap.of(
                                partition(0), offsetDistance(50, 75),
                                partition(1), offsetDistance(75, 100),
                                partition(2), offsetDistance(0, 5)
                        ),
                        ImmutableMap.of(
                                partition(0), offsetDistance(50, 61),
                                partition(1), offsetDistance(75, 87),
                                partition(2), offsetDistance(0, 2)
                        )
                )
        );
    }

    private String buildRatioReport(final Map<Partition, OffsetDistance> partitionOffsetDistance, final Map<Partition, OffsetDistance> actual) {
        final StringBuilder report = new StringBuilder();

        partitionOffsetDistance.forEach((partition, offsetDistance) -> {
            final long maxDistance = offsetDistance.distance();
            final OffsetDistance coveredOffsetDistance = actual.get(partition);
            final long coveredDistance = coveredOffsetDistance.distance();
            report.append(String.format(
                    "\tPartition: '%s' [%3d - %3d] maxDistance: '%3d' [%3d - %3d] coveredDistance: '%3d' ratio '%.2f'",
                    partition.number(),
                    offsetDistance.startOffset().number(),
                    offsetDistance.endOffset().number(),
                    maxDistance,
                    coveredOffsetDistance.startOffset().number(),
                    coveredOffsetDistance.endOffset().number(),
                    coveredDistance,
                    coveredDistance / (double) maxDistance
            ));
            report.append(System.lineSeparator());
        });

        return report.toString();
    }

    static OffsetDistance offsetDistance(final int startOffset, final int endOffset) {
        return OffsetDistance.of(StartOffset.of(startOffset), EndOffset.of(endOffset));
    }

    static Partition partition(final int partition) { return Partition.of(partition); }

    static int bucketSize(final int bucketSize) { return bucketSize; }

    static long distance(@NonNull final Collection<OffsetDistance> offsetDistances) {
        return offsetDistances.stream().map(OffsetDistance::distance).mapToLong(Long::longValue).sum();
    }
}