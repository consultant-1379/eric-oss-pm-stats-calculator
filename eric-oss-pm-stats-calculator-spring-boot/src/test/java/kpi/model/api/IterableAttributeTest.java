/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IterableAttributeTest {
    @Spy
    IterableAttribute<String> objectUnderTest;

    @Test
    void shouldReturnTrueWhenListIsEmpty() {
        List<String> emptyList = List.of();
        when(objectUnderTest.value()).thenReturn(emptyList);
        Assertions.assertThat(objectUnderTest.isEmpty()).isTrue();
        verify(objectUnderTest).value();
    }

    @Test
    void shouldReturnTrueWhenListIsNotEmpty() {
        List<String> populatedList = List.of("first", "second");
        when(objectUnderTest.value()).thenReturn(populatedList);
        Assertions.assertThat(objectUnderTest.isNotEmpty()).isTrue();
        verify(objectUnderTest).value();
    }

    @Test
    void shouldReturnEmptyIteratorWhenListIsEmpty() {
        List<String> emptyList = List.of();
        when(objectUnderTest.value()).thenReturn(emptyList);
        Assertions.assertThat(objectUnderTest.iterator().hasNext()).isFalse();
        verify(objectUnderTest).value();
    }

    @Test
    void shouldReturnPopulatedIteratorWhenListIsNotEmpty() {
        List<String> populatedList = List.of("first");
        when(objectUnderTest.value()).thenReturn(populatedList);
        Assertions.assertThat(objectUnderTest.iterator().hasNext()).isTrue();
        verify(objectUnderTest).value();
    }

    @Test
    void shouldReturnPopulatedStreamWhenListIsNotEmpty() {
        List<String> populatedList = List.of("first");
        when(objectUnderTest.value()).thenReturn(populatedList);
        Assertions.assertThat(objectUnderTest.stream()).hasSameElementsAs(List.of("first"));
        verify(objectUnderTest).value();
    }


}