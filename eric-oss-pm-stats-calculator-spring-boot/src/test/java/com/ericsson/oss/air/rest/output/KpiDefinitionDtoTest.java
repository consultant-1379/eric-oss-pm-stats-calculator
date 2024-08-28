/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.output;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KpiDefinitionDtoTest {
    @Spy KpiDefinitionDto objectUnderTest;

    @Test
    void shouldThrowUnsupportedExceptionOnReexportLateData() {
        assertThatThrownBy(() -> objectUnderTest.reexportLateData())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowUnsupportedExceptionOnDataReliabilityOffset() {
        assertThatThrownBy(() -> objectUnderTest.dataReliabilityOffset())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowUnsupportedExceptionOnDataLookbackLimit() {
        assertThatThrownBy(() -> objectUnderTest.dataLookbackLimit())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowUnsupportedExceptionOnExecutionGroup() {
        assertThatThrownBy(() -> objectUnderTest.executionGroup())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowUnsupportedExceptionOnInpDataIdentifier() {
        assertThatThrownBy(() -> objectUnderTest.inpDataIdentifier())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
