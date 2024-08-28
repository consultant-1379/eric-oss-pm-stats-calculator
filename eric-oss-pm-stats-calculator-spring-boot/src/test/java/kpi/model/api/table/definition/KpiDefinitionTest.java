/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table.definition;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionTest {
    @Spy
    KpiDefinition objectUnderTest;

    @Test
    void shouldThrowExceptionOnDataReliabilityOffset() {
        Assertions.assertThatThrownBy(() -> objectUnderTest.dataReliabilityOffset())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionOnInpDataIdentifier() {
        Assertions.assertThatThrownBy(() -> objectUnderTest.inpDataIdentifier())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionOnExecutionGroup() {
        Assertions.assertThatThrownBy(() -> objectUnderTest.executionGroup())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionOnDataLookBackLimit() {
        Assertions.assertThatThrownBy(() -> objectUnderTest.dataLookBackLimit())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionOnReexportLateData() {
        Assertions.assertThatThrownBy(() -> objectUnderTest.reexportLateData())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}