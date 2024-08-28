/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.registry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.enterprise.inject.Instance;

import com.ericsson.oss.air.pm.stats.calculation.ReliabilityThresholdCalculator;
import com.ericsson.oss.air.pm.stats.calculation.SimpleReliabilityThresholdCalculator;
import com.ericsson.oss.air.pm.stats.calculation.reliability.threshold.registry.exception.ReliabilityThresholdCalculatorNotFound;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReliabilityThresholdCalculatorRegistryTest {

    @Mock
    SimpleReliabilityThresholdCalculator simpleReliabilityThresholdCalculatorMock;

    @Mock
    Instance<ReliabilityThresholdCalculator> reliabilityThresholdCalculatorInstanceMock;

    @InjectMocks
    ReliabilityThresholdCalculatorRegistry objectUnderTest;

    @Test
    void shouldGiveBackCalculator() {
        doReturn(true).when(simpleReliabilityThresholdCalculatorMock).doesSupport(any(KpiType.class));
        doReturn(Stream.of(simpleReliabilityThresholdCalculatorMock)).when(reliabilityThresholdCalculatorInstanceMock).stream();

        final ReliabilityThresholdCalculator calculator = objectUnderTest.calculator(List.of(KpiDefinitionEntity.builder().build()));

        verify(simpleReliabilityThresholdCalculatorMock).doesSupport(any(KpiType.class));
        verify(reliabilityThresholdCalculatorInstanceMock).stream();

        Assertions.assertThat(calculator).isInstanceOf(SimpleReliabilityThresholdCalculator.class);
    }

    @Test
    void shouldThrowExceptionOnNonExistingCalculator() {
        doReturn(Stream.empty()).when(reliabilityThresholdCalculatorInstanceMock).stream();

        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withSchemaDataSpace("data");
        builder.withSchemaCategory("category");
        builder.withSchemaName("name");
        builder.withExecutionGroup(ExecutionGroup.builder().withId(1).withName("exec-group").build());

        final Collection<KpiDefinitionEntity> kpiDefinitions = List.of(builder.build());

        Assertions.assertThatThrownBy(() -> objectUnderTest.calculator(kpiDefinitions))
                .hasMessage("KPI type 'SCHEDULED_SIMPLE' is not supported")
                .isInstanceOf(ReliabilityThresholdCalculatorNotFound.class);
    }
}
