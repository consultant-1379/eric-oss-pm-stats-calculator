/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service._spy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.KpiDefinitionServiceImpl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionServiceImplTest {
    @Spy
    KpiDefinitionServiceImpl objectUnderTest;

    @Captor
    ArgumentCaptor<Function<KpiDefinitionEntity, Object>> kpiDefinitionMapperCaptor;

    @Test
    void shouldFindKpiDefinitionsByExecutionGroup() {
        final List<KpiDefinitionEntity> kpiDefinitions = List.of(kpiDefinition("a"), kpiDefinition("b"));

        doReturn(kpiDefinitions).when(objectUnderTest).findKpiDefinitionsByExecutionGroup("executionGroup");

        final Collection<String> actual = objectUnderTest.findKpiDefinitionsByExecutionGroup("executionGroup", KpiDefinitionEntity::name);

        verify(objectUnderTest).findKpiDefinitionsByExecutionGroup("executionGroup");

        Assertions.assertThat(actual).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void shouldFindKpiDefinitionNamesByExecutionGroup() {
        final List<String> kpiDefinitionNames = List.of("a", "b");

        doReturn(kpiDefinitionNames).when(objectUnderTest).findKpiDefinitionsByExecutionGroup(eq("executionGroup"), any());

        final Collection<String> actual = objectUnderTest.findKpiDefinitionNamesByExecutionGroup("executionGroup");

        verify(objectUnderTest).findKpiDefinitionsByExecutionGroup(eq("executionGroup"), kpiDefinitionMapperCaptor.capture());

        Assertions.assertThat(actual).containsExactlyInAnyOrder("a", "b");

        Assertions.assertThat(kpiDefinitionMapperCaptor.getValue().apply(kpiDefinition("a"))).isEqualTo("a");
    }

    static KpiDefinitionEntity kpiDefinition(final String name) {
        return KpiDefinitionEntity.builder().withName(name).build();
    }
}