/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table.definition.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.element.AggregationElement;

import kpi.model.ondemand.element.OnDemandAggregationElement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AggregationElementsAttributeTest {

    @Spy
    AggregationElementsAttribute<AggregationElement> objectUnderTest;

    @Test
    void shouldReturnAggregationElements() {
        final List<AggregationElement> elements = Stream.of("table.column1", "table.column2").map(OnDemandAggregationElement::of).collect(Collectors.toList());
        when(objectUnderTest.value()).thenReturn(elements);
        final List<String> actual = objectUnderTest.listOfValues();
        Assertions.assertThat(actual).contains("table.column1", "table.column2");
        verify(objectUnderTest).listOfValues();
    }
}
