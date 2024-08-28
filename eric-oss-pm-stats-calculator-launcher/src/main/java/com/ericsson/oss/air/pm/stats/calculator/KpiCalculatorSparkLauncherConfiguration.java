/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Holds configuration values for the PM Stats Calculator spark launcher.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class KpiCalculatorSparkLauncherConfiguration {

    static final Integer SPARK_RETRY_DURATION = 1;
    static final Integer MAX_SPARK_RETRY_ATTEMPTS = 2;
    static final Double SPARK_RETRY_MULTIPLIER = 1.5;
    static final Long SPARK_RETRY_DURATION_VALUE_MS = TimeUnit.MINUTES.toMillis(SPARK_RETRY_DURATION);
}
