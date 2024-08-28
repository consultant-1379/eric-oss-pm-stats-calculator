/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.launcher;

import java.util.concurrent.TimeUnit;

import static com.ericsson.oss.air.pm.stats.common.env.Environment.getEnvironmentValue;

/**
 * Holds configuration values for the {@code AbstractSparkLauncher} spark launcher.
 */
public final class SparkLauncherConfiguration {

    static final String POST_SPARK_EXECUTION_WAIT_TIME_UNIT = getEnvironmentValue("POST_SPARK_EXECUTION_WAIT_TIME_UNIT", TimeUnit.SECONDS.toString());
    static final String POST_SPARK_EXECUTION_WAIT_TIME = getEnvironmentValue("POST_SPARK_EXECUTION_WAIT_TIME", "5");
    static final Long POST_SPARK_EXECUTION_WAIT_VALUE_MS = convertToMilliSeconds(POST_SPARK_EXECUTION_WAIT_TIME_UNIT, POST_SPARK_EXECUTION_WAIT_TIME);

    private SparkLauncherConfiguration() {

    }

    public static long convertToMilliSeconds(final String unit, final String value) {
        return TimeUnit.MILLISECONDS.convert(Long.parseLong(value), TimeUnit.valueOf(unit));
    }
}
