/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.provider;

import org.apache.spark.sql.execution.SparkSqlParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SparkSqlParserProviderTest {
    SparkSqlParserProvider objectUnderTest = new SparkSqlParserProvider();

    @Test
    void shouldProvideSqlParser() {
        final SparkSqlParser actual = objectUnderTest.provideSqlParser();
        Assertions.assertThat(actual).isNotNull();
    }
}