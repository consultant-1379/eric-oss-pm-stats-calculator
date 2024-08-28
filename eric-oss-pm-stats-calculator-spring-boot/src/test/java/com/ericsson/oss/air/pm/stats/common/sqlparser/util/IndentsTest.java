/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.util;

import static java.lang.System.lineSeparator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IndentsTest {

    @Nested
    class IndentMultiline {
        @Test
        void shouldIndentMultiline() {
            final String actual = Indents.indent(String.join(lineSeparator(), "1", "2", "3", "4"), 2, 1);
            Assertions.assertThat(actual).isEqualTo(String.join(
                    lineSeparator(),
                    "  1", "  2", "  3", "  4"
            ));
        }

        @Test
        void shouldIndentWhenNoMultiline() {
            final String actual = Indents.indent("single-line", 2, 3);
            Assertions.assertThat(actual).isEqualTo(" ".repeat(6) + "single-line");
        }
    }

    @Test
    void shouldVerifyOverloadedIndent() {
        final String actual = Indents.indent(2);
        Assertions.assertThat(actual).isEqualTo(" ".repeat(4));
    }

    @Test
    void shouldVerifyBaseIndent() {
        final String actual = Indents.indent(2, 4);
        Assertions.assertThat(actual).isEqualTo(" ".repeat(8));
    }
}