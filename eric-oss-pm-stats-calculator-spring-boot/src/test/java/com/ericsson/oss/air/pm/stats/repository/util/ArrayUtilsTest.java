/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArrayUtilsTest {
    @Test
    void shouldVerifySuccessfullyFinishedStates(@Mock final Connection connectionMock, @Mock final Array arrayMock) throws SQLException {
        when(connectionMock.createArrayOf("VARCHAR", new String[]{"FINISHED", "FINALIZING"})).thenReturn(arrayMock);

        final Array actual = ArrayUtils.successfullyFinishedStates(connectionMock);

        verify(connectionMock).createArrayOf("VARCHAR", new String[]{"FINISHED", "FINALIZING"});

        Assertions.assertThat(actual).isEqualTo(arrayMock);
    }
}