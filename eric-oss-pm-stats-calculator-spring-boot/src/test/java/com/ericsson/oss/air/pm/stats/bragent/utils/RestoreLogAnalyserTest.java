/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.bragent.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RestoreLogAnalyserTest {

    @Test
    void shouldFindErrorInLog() {
        final String testLog = "Warning: Errors ignored the GRANT for ";

        final boolean actual = RestoreLogAnalyser.isLogContainsWarning(testLog);
        assertThat(actual).isTrue();
    }

    @Test
    void shouldFindErrorInOutput() {
        final String testLog = "ERROR:  duplicate key value violates";

        final boolean actual = RestoreLogAnalyser.isLogContainsDuplicateKeyValueError(testLog);
        assertThat(actual).isTrue();
    }

    @Test
    void shouldNoErrorInErrorLog() {
        final String testLog = "Table test is created";

        final boolean actual = RestoreLogAnalyser.isLogContainsWarning(testLog);
        assertThat(actual).isFalse();
    }

    @Test
    void shouldFindNoErrorInOutput() {
        final String testLog = "Table test is created";

        final boolean actual = RestoreLogAnalyser.isLogContainsDuplicateKeyValueError(testLog);
        assertThat(actual).isFalse();
    }

    @Test
    void shouldFindErrorInBackupLog() {
        final String testLog = "connection to database \"testDB\" failed";

        final boolean actual = RestoreLogAnalyser.isLogContainsFailedConnectionError(testLog);
        assertThat(actual).isTrue();
    }

    @Test
    void shouldFindNoErrorInBackupLog() {
        final String testLog = "";

        final boolean actual = RestoreLogAnalyser.isLogContainsFailedConnectionError(testLog);
        assertThat(actual).isFalse();
    }
}