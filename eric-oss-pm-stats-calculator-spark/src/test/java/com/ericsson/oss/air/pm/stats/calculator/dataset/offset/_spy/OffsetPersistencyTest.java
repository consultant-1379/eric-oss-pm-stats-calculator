/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.offset._spy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ericsson.oss.air.pm.stats.calculator.dataset.offset.OffsetPersistency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetPersistencyTest {

    @Spy OffsetPersistency objectUnderTest;

    @Test
    void shouldThrowExceptionWhenUpsertingOffset() {
        assertThatThrownBy(() -> objectUnderTest.upsertOffset(null))
                .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionWhenCalculateMaxOffsetsForSourceTables () {
        assertThatThrownBy(() -> objectUnderTest.calculateMaxOffsetsForSourceTables(null, null,0))
                .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionWhenPersistMaxOffsets () {
        assertThatThrownBy(() -> objectUnderTest.persistMaxOffsets(0))
                .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

}