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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.pm.stats.graph.DependencyFinder;
import com.ericsson.oss.air.pm.stats.graph.KpiGroupLoopDetector;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionGraph;
import com.ericsson.oss.air.pm.stats.graph.model.KpiDefinitionVertex;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.exception.KpiDefinitionValidationException;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;

import kpi.model.KpiDefinitionRequest;
import kpi.model.ScheduledComplex;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoopValidatorTest {
    static final String KPI_NAME = "kpiName";

    @Mock
    private DependencyFinder dependencyFinderMock;
    @Mock
    private KpiGroupLoopDetector kpiGroupLoopDetectorMock;
    @Mock
    private KpiDefinitionService kpiDefinitionServiceMock;

    @InjectMocks
    LoopValidator objectUnderTest;

    @Test
    void shouldNotValidateWhenCircleInGroupDefinitions(@Mock final KpiDefinitionRequest kpiDefinitionMock,
                                                       @Mock final ScheduledComplex scheduledComplexMock,
                                                       @Mock final KpiDefinitionGraph kpiDefinitionGraphMock) {
        final KpiDefinitionVertex green0 = createVertex("green", 0);
        final KpiDefinitionVertex green1 = createVertex("green", 1);
        final KpiDefinitionVertex green3 = createVertex("green", 3);
        final KpiDefinitionVertex yellow2 = createVertex("yellow", 2);
        final KpiDefinitionVertex pink4 = createVertex("pink", 4);

        when(kpiDefinitionMock.scheduledComplex()).thenReturn(scheduledComplexMock);
        when(scheduledComplexMock.isEmpty()).thenReturn(false);
        when(dependencyFinderMock.dependencyFinder(scheduledComplexMock)).thenReturn(kpiDefinitionGraphMock);
        when(kpiGroupLoopDetectorMock.collectLoopsOnExecutionGroups(kpiDefinitionGraphMock)).thenReturn(Collections.singletonList(List.of(green0, green1, pink4, yellow2, green3)));

        assertThatThrownBy(() -> objectUnderTest.validateNoCircle(kpiDefinitionMock))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("Following execution groups have circular definition: [green.1 -> pink.4 -> yellow.2 -> green.3]");

        verify(kpiDefinitionMock).scheduledComplex();
        verify(scheduledComplexMock).isEmpty();
        verify(kpiGroupLoopDetectorMock).collectLoopsOnExecutionGroups(kpiDefinitionGraphMock);
        verify(dependencyFinderMock).dependencyFinder(scheduledComplexMock);
    }

    @Test
    void shouldValidateWhenNoCircle(@Mock final KpiDefinitionRequest kpiDefinitionMock,
                                    @Mock final ScheduledComplex scheduledComplexMock,
                                    @Mock final KpiDefinitionGraph kpiDefinitionGraphMock) {

        when(kpiDefinitionMock.scheduledComplex()).thenReturn(scheduledComplexMock);
        when(scheduledComplexMock.isEmpty()).thenReturn(false);
        when(dependencyFinderMock.dependencyFinder(scheduledComplexMock)).thenReturn(kpiDefinitionGraphMock);
        when(kpiGroupLoopDetectorMock.collectLoopsOnExecutionGroups(kpiDefinitionGraphMock)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> objectUnderTest.validateNoCircle(kpiDefinitionMock));

        verify(kpiDefinitionMock).scheduledComplex();
        verify(scheduledComplexMock).isEmpty();
        verify(kpiGroupLoopDetectorMock).collectLoopsOnExecutionGroups(kpiDefinitionGraphMock);
        verify(dependencyFinderMock).dependencyFinder(scheduledComplexMock);
    }

    @Test
    void shouldReturnOnNoComplexDefinitions(@Mock final KpiDefinitionRequest kpiDefinitionMock,
                                            @Mock ScheduledComplex scheduledComplexMock) {

        when(kpiDefinitionMock.scheduledComplex()).thenReturn(scheduledComplexMock);
        when(scheduledComplexMock.isEmpty()).thenReturn(true);

        objectUnderTest.validateNoCircle(kpiDefinitionMock);

        verify(kpiDefinitionMock).scheduledComplex();
        verifyNoInteractions(dependencyFinderMock, kpiGroupLoopDetectorMock);
    }

    @Test
    void shouldValidateWhenCircleInSameExecutionGroup(
            @Mock final KpiDefinitionRequest kpiDefinitionMock,
            @Mock final ScheduledComplex scheduledComplexMock,
            @Mock final KpiDefinitionGraph kpiDefinitionGraphMock
    ) {
        final KpiDefinitionVertex green0 = createVertex("green", 0);
        final KpiDefinitionVertex green1 = createVertex("green", 1);
        final KpiDefinitionVertex green3 = createVertex("green", 3);

        when(kpiDefinitionMock.scheduledComplex()).thenReturn(scheduledComplexMock);
        when(scheduledComplexMock.isEmpty()).thenReturn(false);
        when(dependencyFinderMock.dependencyFinder(scheduledComplexMock)).thenReturn(kpiDefinitionGraphMock);
        when(kpiGroupLoopDetectorMock.collectLoopsOnKpiDefinitions(kpiDefinitionGraphMock)).thenReturn(Collections.singletonList(List.of(green0, green1, green3, green0)));

        assertThatThrownBy(() -> objectUnderTest.validateNoCircle(kpiDefinitionMock))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("Following KPIs in the same group have circular definition: [green.0 -> green.1 -> green.3 -> green.0]");

        verify(kpiDefinitionMock).scheduledComplex();
        verify(scheduledComplexMock).isEmpty();
        verify(kpiGroupLoopDetectorMock).collectLoopsOnKpiDefinitions(kpiDefinitionGraphMock);
        verify(dependencyFinderMock).dependencyFinder(scheduledComplexMock);
    }

    @Test
    void shouldValidateWhenNoCircleInSameExecutionGroup(
            @Mock final KpiDefinitionRequest kpiDefinitionMock,
            @Mock final ScheduledComplex scheduledComplexMock,
            @Mock final KpiDefinitionGraph kpiDefinitionGraphMock
    ) {
        when(kpiDefinitionMock.scheduledComplex()).thenReturn(scheduledComplexMock);
        when(scheduledComplexMock.isEmpty()).thenReturn(false);
        when(dependencyFinderMock.dependencyFinder(scheduledComplexMock)).thenReturn(kpiDefinitionGraphMock);
        when(kpiGroupLoopDetectorMock.collectLoopsOnKpiDefinitions(kpiDefinitionGraphMock)).thenReturn(Collections.emptyList());

        Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.validateNoCircle(kpiDefinitionMock));

        verify(kpiDefinitionMock).scheduledComplex();
        verify(scheduledComplexMock).isEmpty();
        verify(kpiGroupLoopDetectorMock).collectLoopsOnKpiDefinitions(kpiDefinitionGraphMock);
        verify(dependencyFinderMock).dependencyFinder(scheduledComplexMock);
    }

    @Test
    void shouldNotValidateWhenCircleInGroupDefinitions(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock1,
                                                       @Mock final KpiDefinitionEntity kpiDefinitionEntityMock2,
                                                       @Mock final KpiDefinitionGraph kpiDefinitionGraphMock) {
        final KpiDefinitionVertex green0 = createVertex("green", 0);
        final KpiDefinitionVertex green1 = createVertex("green", 1);
        final KpiDefinitionVertex green3 = createVertex("green", 3);
        final KpiDefinitionVertex yellow2 = createVertex("yellow", 2);
        final KpiDefinitionVertex pink4 = createVertex("pink", 4);

        when(kpiDefinitionServiceMock.findComplexKpis()).thenReturn(List.of(kpiDefinitionEntityMock1, kpiDefinitionEntityMock2));
        when(kpiDefinitionEntityMock1.name()).thenReturn("");
        when(kpiDefinitionEntityMock2.name()).thenReturn(KPI_NAME);
        when(dependencyFinderMock.dependencyFinder(anySet())).thenReturn(kpiDefinitionGraphMock);
        when(kpiGroupLoopDetectorMock.collectLoopsOnExecutionGroups(kpiDefinitionGraphMock)).thenReturn(Collections.singletonList(List.of(green0, green1, pink4, yellow2, green3)));

        assertThatThrownBy(() -> objectUnderTest.validateNoCircle(kpiDefinitionEntityMock1))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("Following execution groups have circular definition: [green.1 -> pink.4 -> yellow.2 -> green.3]");

        verify(kpiDefinitionServiceMock).findComplexKpis();
        verify(dependencyFinderMock).dependencyFinder(anySet());
        verify(kpiGroupLoopDetectorMock).collectLoopsOnExecutionGroups(kpiDefinitionGraphMock);
    }

    @Test
    void shouldNotValidateWhenCircleInAGroup(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock1,
                                             @Mock final KpiDefinitionEntity kpiDefinitionEntityMock2,
                                             @Mock final KpiDefinitionGraph kpiDefinitionGraphMock) {
        final KpiDefinitionVertex green0 = createVertex("green", 0);
        final KpiDefinitionVertex green1 = createVertex("green", 1);
        final KpiDefinitionVertex green3 = createVertex("green", 3);

        when(kpiDefinitionServiceMock.findComplexKpis()).thenReturn(List.of(kpiDefinitionEntityMock1, kpiDefinitionEntityMock2));
        when(kpiDefinitionEntityMock1.name()).thenReturn("");
        when(kpiDefinitionEntityMock2.name()).thenReturn(KPI_NAME);
        when(dependencyFinderMock.dependencyFinder(anySet())).thenReturn(kpiDefinitionGraphMock);
        when(kpiGroupLoopDetectorMock.collectLoopsOnKpiDefinitions(kpiDefinitionGraphMock)).thenReturn(Collections.singletonList(List.of(green0, green1, green3, green0)));

        assertThatThrownBy(() -> objectUnderTest.validateNoCircle(kpiDefinitionEntityMock1))
                .isInstanceOf(KpiDefinitionValidationException.class)
                .hasMessage("Following KPIs in the same group have circular definition: [green.0 -> green.1 -> green.3 -> green.0]");

        verify(kpiDefinitionServiceMock).findComplexKpis();
        verify(dependencyFinderMock).dependencyFinder(anySet());
        verify(kpiGroupLoopDetectorMock).collectLoopsOnExecutionGroups(kpiDefinitionGraphMock);
    }

    @Test
    void shouldValidateWhenNoCircle(@Mock final KpiDefinitionEntity kpiDefinitionEntityMock1,
                                    @Mock final KpiDefinitionEntity kpiDefinitionEntityMock2,
                                    @Mock final KpiDefinitionGraph kpiDefinitionGraphMock) {
        when(kpiDefinitionServiceMock.findComplexKpis()).thenReturn(List.of(kpiDefinitionEntityMock1, kpiDefinitionEntityMock2));
        when(kpiDefinitionEntityMock1.name()).thenReturn("");
        when(kpiDefinitionEntityMock2.name()).thenReturn(KPI_NAME);
        when(dependencyFinderMock.dependencyFinder(anySet())).thenReturn(kpiDefinitionGraphMock);
        when(kpiGroupLoopDetectorMock.collectLoopsOnExecutionGroups(kpiDefinitionGraphMock)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> objectUnderTest.validateNoCircle(kpiDefinitionEntityMock1));

        verify(kpiDefinitionServiceMock).findComplexKpis();
        verify(dependencyFinderMock).dependencyFinder(anySet());
        verify(kpiGroupLoopDetectorMock).collectLoopsOnExecutionGroups(kpiDefinitionGraphMock);
    }

    static KpiDefinitionVertex createVertex(final String color, final Integer number) {
        return KpiDefinitionVertex.builder().executionGroup(color).definitionName(String.valueOf(number)).build();
    }

}