/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api;

import static java.util.UUID.fromString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.exception.EntityNotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculationRepositoryTest {
    static final UUID UUID = fromString("912ba411-ec57-4ff3-a274-46a30cac8382");

    @Spy
    CalculationRepository objectUnderTest;

    @Nested
    class forceFetchExecutionGroupByCalculationId {
        @Test
        void shouldForceFetch() {
            doReturn(Optional.of("executionGroup")).when(objectUnderTest).findExecutionGroupByCalculationId(UUID);

            final String actual = objectUnderTest.forceFetchExecutionGroupByCalculationId(UUID);

            verify(objectUnderTest).findExecutionGroupByCalculationId(UUID);

            Assertions.assertThat(actual).isEqualTo("executionGroup");
        }

        @Test
        void shouldRaiseException_whenNotFoundByCalculationId() {
            doReturn(Optional.empty()).when(objectUnderTest).findExecutionGroupByCalculationId(UUID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.forceFetchExecutionGroupByCalculationId(UUID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Kpi calculation with calculation id '912ba411-ec57-4ff3-a274-46a30cac8382' not found.");

            verify(objectUnderTest).findExecutionGroupByCalculationId(UUID);
        }
    }

    @Nested
    class forceFetchKpiTypeByCalculationId {

        @Test
        void shouldRaiseException_whenNotFoundByCalculationId() {
            doReturn(Optional.empty()).when(objectUnderTest).findKpiTypeByCalculationId(UUID);

            Assertions.assertThatThrownBy(() -> objectUnderTest.forceFetchKpiTypeByCalculationId(UUID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Kpi calculation with calculation id '912ba411-ec57-4ff3-a274-46a30cac8382' not found.");

            verify(objectUnderTest).findKpiTypeByCalculationId(UUID);
        }

        @Test
        void shouldForceFetch() {
            doReturn(Optional.of(KpiType.valueOf("SCHEDULED_SIMPLE"))).when(objectUnderTest).findKpiTypeByCalculationId(UUID);

            final KpiType actual = objectUnderTest.forceFetchKpiTypeByCalculationId(UUID);

            verify(objectUnderTest).findKpiTypeByCalculationId(UUID);

            Assertions.assertThat(actual).isEqualTo(KpiType.valueOf("SCHEDULED_SIMPLE"));
        }
    }
}