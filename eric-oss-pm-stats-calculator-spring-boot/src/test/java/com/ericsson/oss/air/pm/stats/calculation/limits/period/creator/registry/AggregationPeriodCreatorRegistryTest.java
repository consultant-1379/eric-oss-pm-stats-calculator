/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import javax.enterprise.inject.Instance;

import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.api.AggregationPeriodCreator;
import com.ericsson.oss.air.pm.stats.calculation.limits.period.creator.registry.exception.AggregationPeriodNotSupportedException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregationPeriodCreatorRegistryTest {
    @Mock
    Instance<AggregationPeriodCreator> aggregationPeriodCreatorsMock;

    @InjectMocks
    AggregationPeriodCreatorRegistry objectUnderTest;

    @Test
    void shouldFindAggregationCreator(@Mock final AggregationPeriodCreator aggregationPeriodCreatorMock) {
        when(aggregationPeriodCreatorsMock.stream()).thenReturn(Stream.of(aggregationPeriodCreatorMock));
        when(aggregationPeriodCreatorMock.doesSupport(60L)).thenReturn(true);

        final AggregationPeriodCreator actual = objectUnderTest.aggregationPeriod(60L);

        verify(aggregationPeriodCreatorsMock).stream();
        verify(aggregationPeriodCreatorMock).doesSupport(60L);

        Assertions.assertThat(actual).isEqualTo(aggregationPeriodCreatorMock);
    }

    @Test
    void shouldNotFindAggregationCreator() {
        when(aggregationPeriodCreatorsMock.stream()).thenReturn(Stream.empty());

        Assertions.assertThatThrownBy(() -> objectUnderTest.aggregationPeriod(60L))
                .isInstanceOf(AggregationPeriodNotSupportedException.class)
                .hasMessage("Aggregation period '%s' is not supported", 60);

        verify(aggregationPeriodCreatorsMock).stream();
    }
}