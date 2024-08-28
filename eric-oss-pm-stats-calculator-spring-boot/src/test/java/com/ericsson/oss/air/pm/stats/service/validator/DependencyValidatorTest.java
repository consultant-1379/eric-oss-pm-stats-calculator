/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Sets.newLinkedHashSet;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.graph.DependencyFinder;
import com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DependencyValidatorTest {
    @Mock
    DependencyFinder dependencyFinderMock;

    @InjectMocks
    DependencyValidator objectUnderTest;

    @Test
    void whenNoDanglingDependenciesFound_shouldNotThrow(@Mock List<String> kpiNamesMock) {
        when(dependencyFinderMock.findInadequateDependencies(kpiNamesMock, DEFAULT_COLLECTION_ID)).thenReturn(Collections.emptyMap());

        assertThatNoException()
                .isThrownBy(() -> objectUnderTest.validateNoDanglingDependenciesAfterDeletingDefinitions(kpiNamesMock, DEFAULT_COLLECTION_ID));
    }

    @Test
    void whenDanglingDependenciesFound_shouldThrow(@Mock List<String> kpiNamesMock) {
        when(dependencyFinderMock.findInadequateDependencies(kpiNamesMock, DEFAULT_COLLECTION_ID)).thenReturn(Map.of("kpiName", newLinkedHashSet("dep1", "dep2")));

        assertThatThrownBy(() -> objectUnderTest.validateNoDanglingDependenciesAfterDeletingDefinitions(kpiNamesMock, DEFAULT_COLLECTION_ID))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("The Following KPIs have dependencies that would be deleted: [kpiName: [dep1, dep2]]");
    }
}