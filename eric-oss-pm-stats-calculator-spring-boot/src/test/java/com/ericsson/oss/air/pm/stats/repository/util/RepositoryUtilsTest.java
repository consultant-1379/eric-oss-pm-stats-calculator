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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.SQLException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RepositoryUtilsTest {
    @Test
    void shouldParseArray_withNull() throws SQLException {
        final List<String> actual = RepositoryUtils.parseArrayToString(null);
        Assertions.assertThat(actual).isNull();
    }

    @Test
    void shouldParseArray_withValue() throws SQLException {
        final Array arrayMock = mock(Array.class);

        final String value1 = "value_1";
        final String value2 = "value_2";

        when(arrayMock.getArray()).thenReturn(new Object[]{value1, value2});

        final List<String> actual = RepositoryUtils.parseArrayToString(arrayMock);

        verify(arrayMock).getArray();

        Assertions.assertThat(actual).containsExactlyInAnyOrder(value1, value2);
    }
}