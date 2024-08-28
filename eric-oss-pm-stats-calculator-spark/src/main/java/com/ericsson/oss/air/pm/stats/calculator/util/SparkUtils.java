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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SparkUtils {
    private static final String DESCRIPTION_SEPARATOR = " : ";

    /**
     * Creates a spark job description to be used as a label for spark job metrics.
     *
     * @param metricName
     *            the metric name that corresponds to the spark job
     * @param otherValues
     *            other values which are to be used in the job description
     * @return the job description
     */
    public static String buildJobDescription(final String metricName, final String... otherValues) {
        final StringBuilder description = new StringBuilder(metricName);
        for (final String value : otherValues) {
            description.append(DESCRIPTION_SEPARATOR).append(value);
        }
        return description.toString();
    }

}
