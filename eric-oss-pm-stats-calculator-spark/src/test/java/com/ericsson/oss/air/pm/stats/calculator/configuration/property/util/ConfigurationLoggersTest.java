/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration.property.util;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.configuration.property.util.ConfigurationLoggers.NestedConfiguration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigurationLoggersTest {
    @Test
    void shouldVerifyNestedClassDescription() {
        final String actual = ConfigurationLoggers.createLog(new Level1());

        Assertions.assertThat(actual).isEqualTo(String.join(System.lineSeparator(),
                "\targ1 = 1",
                "\targ2 = 2",
                "\targ3",
                "\t\targ1 = PT0S",
                "\t\targ2 = [4]",
                "\t\targ3",
                "\t\t\targ1 = 1",
                "\t\targ4 = 5",
                "\targ4 = 4"
        ));
    }
}

@SuppressWarnings("unused")
class Level1 {
    private final Integer arg1 = 1;
    private final String arg2 = "2";
    @NestedConfiguration private final Level2 arg3 = new Level2();
    private final Integer arg4 = 4;
    private static final Integer arg5 = 15; /* static field is not logged */

    class Level2 {
        private final Duration arg1 = Duration.ZERO;
        private final List<String> arg2 = Collections.singletonList("4");
        @NestedConfiguration private final Level3 arg3 = new Level3();
        private final Integer arg4 = 5;

        class Level3 {
            private final Integer arg1 = 1;
        }
    }
}