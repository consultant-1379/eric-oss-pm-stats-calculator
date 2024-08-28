/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset._spy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;

import com.ericsson.oss.air.pm.stats.calculator.model.CalculationTimeWindow;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetHandler;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OffsetHandlerTest {

    @Spy OffsetHandler objectUnderTest;

    @Test
    void getCalculationTimeWindow() {
        final Timestamp start = new Timestamp(0);
        final Timestamp end = new Timestamp(10);

        doReturn(start).when(objectUnderTest).getStartTimestamp(60);
        doReturn(end).when(objectUnderTest).getEndTimestamp(60);

        final CalculationTimeWindow actual = objectUnderTest.getCalculationTimeWindow(60);

        verify(objectUnderTest).getStartTimestamp(60);
        verify(objectUnderTest).getEndTimestamp(60);

        Assertions.assertThat(actual).isEqualTo(CalculationTimeWindow.of(start, end));
    }
}