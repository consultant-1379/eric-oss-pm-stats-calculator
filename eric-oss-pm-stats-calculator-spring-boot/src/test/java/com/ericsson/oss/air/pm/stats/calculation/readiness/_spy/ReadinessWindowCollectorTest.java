/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculation.readiness.ReadinessWindowCollector;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadinessWindowCollectorTest {
    @Spy
    ReadinessWindowCollector objectUnderTest;

    @Test
    void shouldCollect(@Mock final Map<DefinitionName, List<ReadinessWindow>> expectedMock) {
        doReturn(expectedMock).when(objectUnderTest).collect("Complex", List.of("A", "B"));

        final Map<DefinitionName, List<ReadinessWindow>> actual = objectUnderTest.collect("Complex", List.of(
                kpiDefinition("A"),
                kpiDefinition("B"))
        );

        verify(objectUnderTest).collect("Complex", List.of("A", "B"));

        Assertions.assertThat(actual).isEqualTo(expectedMock);
    }

    private static KpiDefinitionEntity kpiDefinition(final String name) {
        return KpiDefinitionEntity.builder().withName(name).build();
    }
}