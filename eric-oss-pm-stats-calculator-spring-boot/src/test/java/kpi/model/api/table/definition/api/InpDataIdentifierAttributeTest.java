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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InpDataIdentifierAttributeTest {

    @Spy
    InpDataIdentifierAttribute objectUnderTest;

    @Test
    void shouldReturnDataSpace() {
        final String identifier = "dataSpace|category1|schema2";
        when(objectUnderTest.value()).thenReturn(identifier);
        final String actual = objectUnderTest.dataSpace();
        Assertions.assertThat(actual).isEqualTo("dataSpace");
        verify(objectUnderTest).dataSpace();
    }

    @Test
    void shouldReturnCategory() {
        final String identifier = "dataSpace|category1|schema2";
        when(objectUnderTest.value()).thenReturn(identifier);
        final String actual = objectUnderTest.category();
        Assertions.assertThat(actual).isEqualTo("category1");
        verify(objectUnderTest).category();
    }

    @Test
    void shouldReturnSchema() {
        final String identifier = "dataSpace|category1|schema2";
        when(objectUnderTest.value()).thenReturn(identifier);
        final String actual = objectUnderTest.schema();
        Assertions.assertThat(actual).isEqualTo("schema2");
        verify(objectUnderTest).schema();
    }

}
