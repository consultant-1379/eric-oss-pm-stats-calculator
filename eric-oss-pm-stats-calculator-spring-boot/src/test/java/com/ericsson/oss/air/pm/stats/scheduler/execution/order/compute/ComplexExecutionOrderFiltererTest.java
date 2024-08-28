/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.execution.order.compute;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.ericsson.oss.air.pm.stats.calculation.limits.ReadinessBoundCalculator;
import com.ericsson.oss.air.pm.stats.graph.utils.GraphUtils;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.model.ComplexExecutionOrder;

import org.assertj.core.api.Assertions;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComplexExecutionOrderFiltererTest {
    @Mock
    ReadinessBoundCalculator readinessBoundCalculatorMock;

    @InjectMocks
    ComplexExecutionOrderFilterer objectUnderTest;

    @Test
    void shouldFilterExecutionOrder(@Mock final Graph<String, DefaultEdge> graphMock) {
        try (final MockedStatic<GraphUtils> graphUtilsMockedStatic = mockStatic(GraphUtils.class)) {

            final ComplexExecutionOrder complexExecutionOrder = ComplexExecutionOrder.of(graphMock, order("0", "1", "2", "3"));

            when(readinessBoundCalculatorMock.isReliablyCalculableGroup("0")).thenReturn(true);
            when(readinessBoundCalculatorMock.isReliablyCalculableGroup("1")).thenReturn(true);
            when(readinessBoundCalculatorMock.isReliablyCalculableGroup("2")).thenReturn(false);
            graphUtilsMockedStatic.when(verifyFindAllParentsFromVertex(graphMock, "2")).thenReturn(List.of("2", "3"));

            final Queue<String> actual = objectUnderTest.filterExecutionOrder(complexExecutionOrder);

            verify(readinessBoundCalculatorMock).isReliablyCalculableGroup("0");
            verify(readinessBoundCalculatorMock).isReliablyCalculableGroup("1");
            verify(readinessBoundCalculatorMock).isReliablyCalculableGroup("2");
            graphUtilsMockedStatic.verify(verifyFindAllParentsFromVertex(graphMock, "2"));
            verify(readinessBoundCalculatorMock, never()).isReliablyCalculableGroup("3");

            Assertions.assertThat(actual).containsExactly("0", "1");
        }
    }

    @Test
    void shouldRemoveAll(@Mock final Graph<String, DefaultEdge> graphMock) {
        try (final MockedStatic<GraphUtils> graphUtilsMockedStatic = mockStatic(GraphUtils.class)) {
            final ComplexExecutionOrder complexExecutionOrder = ComplexExecutionOrder.of(graphMock, order("0", "1", "2", "3"));
            graphUtilsMockedStatic.when(verifyFindAllParentsFromVertex(graphMock, "0")).thenReturn(List.of("0", "1", "2"));
            graphUtilsMockedStatic.when(verifyFindAllParentsFromVertex(graphMock, "3")).thenReturn(List.of("3"));

            when(readinessBoundCalculatorMock.isReliablyCalculableGroup("0")).thenReturn(false);
            when(readinessBoundCalculatorMock.isReliablyCalculableGroup("3")).thenReturn(false);

            final Queue<String> actual = objectUnderTest.filterExecutionOrder(complexExecutionOrder);

            verify(readinessBoundCalculatorMock).isReliablyCalculableGroup("0");
            verify(readinessBoundCalculatorMock).isReliablyCalculableGroup("3");
            graphUtilsMockedStatic.verify(verifyFindAllParentsFromVertex(graphMock, "0"));
            graphUtilsMockedStatic.verify(verifyFindAllParentsFromVertex(graphMock, "3"));
            verify(readinessBoundCalculatorMock, never()).isReliablyCalculableGroup("1");
            verify(readinessBoundCalculatorMock, never()).isReliablyCalculableGroup("2");

            Assertions.assertThat(actual).isEmpty();
        }
    }

    @Test
    void shouldReturnTheOrderedList(@Mock final Graph<String, DefaultEdge> graphMock) {
        final ComplexExecutionOrder complexExecutionOrder = ComplexExecutionOrder.of(graphMock, order("0", "1", "2", "3"));

        when(readinessBoundCalculatorMock.isReliablyCalculableGroup("0")).thenReturn(true);
        when(readinessBoundCalculatorMock.isReliablyCalculableGroup("1")).thenReturn(true);
        when(readinessBoundCalculatorMock.isReliablyCalculableGroup("2")).thenReturn(true);
        when(readinessBoundCalculatorMock.isReliablyCalculableGroup("3")).thenReturn(true);

        final Queue<String> actual = objectUnderTest.filterExecutionOrder(complexExecutionOrder);

        verify(readinessBoundCalculatorMock).isReliablyCalculableGroup("0");
        verify(readinessBoundCalculatorMock).isReliablyCalculableGroup("1");
        verify(readinessBoundCalculatorMock).isReliablyCalculableGroup("2");
        verify(readinessBoundCalculatorMock).isReliablyCalculableGroup("3");

        Assertions.assertThat(actual).containsExactly("0", "1", "2", "3");

    }

    static Queue<String> order(final String... orderedVertexes) {
        return new LinkedList<>(List.of(orderedVertexes));
    }

    static Verification verifyFindAllParentsFromVertex(final Graph<String, DefaultEdge> graph, final String startVertex) {
        return () -> GraphUtils.findAllParentsFromVertex(graph, startVertex);
    }
}