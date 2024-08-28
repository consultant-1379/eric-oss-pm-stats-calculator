/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FunctionUtilsTest {
    @Test
    void shouldCreateNewCollection() {
        final Function<Integer, List<String>> function = FunctionUtils.newCollection(LinkedList::new);
        final List<String> actual = function.apply(1);

        Assertions.assertThat(actual).isEmpty();
    }
}