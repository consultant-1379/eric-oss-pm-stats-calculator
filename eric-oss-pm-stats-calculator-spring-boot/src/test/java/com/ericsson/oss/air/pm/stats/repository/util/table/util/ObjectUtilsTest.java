/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjectUtilsTest {

    private static final String ERROR_MESSAGE = "errorMessage";

    @Test
    void shouldReturnStringValueOf() {
        final String actual = ObjectUtils.nonNull("Hello Everyone", () -> ERROR_MESSAGE);
        Assertions.assertThat(actual).isEqualTo("Hello Everyone");
    }

    @Test
    void shouldThrowNullPointerException() {
        Assertions.assertThatThrownBy(() -> ObjectUtils.nonNull(null, () -> ERROR_MESSAGE))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ERROR_MESSAGE);
    }
}