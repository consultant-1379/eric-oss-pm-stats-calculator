/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.start.time.registry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import javax.enterprise.inject.Instance;

import com.ericsson.oss.air.pm.stats.calculation.start.time.CalculationStartTimeCalculator;
import com.ericsson.oss.air.pm.stats.calculation.start.time.api.StartTimeCalculator;
import com.ericsson.oss.air.pm.stats.calculation.start.time.registry.exception.StartTimeCalculatorNotFound;
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
class StartTimeCalculatorRegistryTest {
    @Mock
    CalculationStartTimeCalculator simpleStartTimeCalculatorMock;
    @Mock
    Instance<StartTimeCalculator> startTimeCalculatorInstanceMock;

    @InjectMocks
    StartTimeCalculatorRegistry objectUnderTest;

    @Test
    void shouldGiveBackStartTimeCalculator() {
        doReturn(true).when(simpleStartTimeCalculatorMock).doesSupport(any(KpiType.class));
        doReturn(Stream.of(simpleStartTimeCalculatorMock)).when(startTimeCalculatorInstanceMock).stream();

        final StartTimeCalculator calculator = objectUnderTest.calculator(List.of(KpiDefinitionEntity.builder().build()));

        verify(simpleStartTimeCalculatorMock).doesSupport(any(KpiType.class));
        verify(startTimeCalculatorInstanceMock).stream();

        Assertions.assertThat(calculator).isInstanceOf(CalculationStartTimeCalculator.class);
    }

    @Test
    void shouldThrowException_whenNonExistingCalculator() {
        doReturn(Stream.empty()).when(startTimeCalculatorInstanceMock).stream();

        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withSchemaDataSpace("data");
        builder.withSchemaCategory("category");
        builder.withSchemaName("name");
        builder.withExecutionGroup(ExecutionGroup.builder().withId(1).withName("exec-group").build());

        final Collection<KpiDefinitionEntity> kpiDefinitions = List.of(builder.build());

        Assertions.assertThatThrownBy(() -> objectUnderTest.calculator(kpiDefinitions))
                .hasMessage("KPI type 'SCHEDULED_SIMPLE' is not supported")
                .isInstanceOf(StartTimeCalculatorNotFound.class);
    }
}
