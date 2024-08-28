/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler.logging;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;

class ContextLogCreatorsTest {
    @Test
    void shouldVerifyContextLogCreation() {
        final String actual = ContextLogCreators.createContextLog(new JobDataMap(Map.of(
                "key_1", "value_1",
                "key_2", "value_2"
        )));

        Assertions.assertThat(actual).isEqualTo("Values:%1$s" +
                "\tkey_2 = value_2%1$s" +
                "\tkey_1 = value_1%1$s",
                System.lineSeparator()
        );
    }
}