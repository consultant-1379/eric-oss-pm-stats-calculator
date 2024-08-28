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

import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;

import kpi.model.ondemand.element.OnDemandFilterElement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FiltersAttributeTest {

    @Spy
    FiltersAttribute<FilterElement> objectUnderTest;

    @Test
    void shouldReturnFilters() {
        final List<FilterElement> filters = Stream.of("filter1", "filter2").map(OnDemandFilterElement::of).collect(Collectors.toList());
        when(objectUnderTest.value()).thenReturn(filters);
        final List<String> actual = objectUnderTest.listOfValues();
        Assertions.assertThat(actual).contains("filter1", "filter2");
        verify(objectUnderTest).listOfValues();
    }
}
