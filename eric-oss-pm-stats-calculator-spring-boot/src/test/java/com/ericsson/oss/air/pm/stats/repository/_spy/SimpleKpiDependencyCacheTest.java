/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.repository.SimpleKpiDependencyCache;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimpleKpiDependencyCacheTest {
    @Spy
    SimpleKpiDependencyCache objectUnderTest;

    @Test
    void shouldVerifyTransitiveDependencyNamesOf() {
        final List<String> kpiDefinitionNames = List.of("a", "b");
        final Map<String, Set<KpiDefinitionEntity>> dependencies = Map.of(
                "a", Set.of(kpiDefinitionEntity("c", "execution_group_1"), kpiDefinitionEntity("d", "execution_group_2")),
                "b", Set.of(kpiDefinitionEntity("c", "execution_group_1"), kpiDefinitionEntity("e", "execution_group_1"))
        );

        doReturn(dependencies).when(objectUnderTest).loadDependenciesForMultipleDefinitions(kpiDefinitionNames);

        final Collection<String> actual = objectUnderTest.transitiveDependencyExecutionGroupsOf(kpiDefinitionNames);

        verify(objectUnderTest).loadDependenciesForMultipleDefinitions(kpiDefinitionNames);

        Assertions.assertThat(actual).containsExactlyInAnyOrder("execution_group_1", "execution_group_2");
    }

    static KpiDefinitionEntity kpiDefinitionEntity(final String name, final String executionGroup) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withExecutionGroup(ExecutionGroup.builder().withName(executionGroup).build());
        return builder.build();
    }

}