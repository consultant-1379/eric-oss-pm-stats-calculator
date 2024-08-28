/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a utility class that provides port numbers to Spark workers. These ports are used by the JMX executor to scrape metrics and must not
 * conflict with other running spark jobs ports. MAXIMUM_CONCURRENT_CALCULATIONS is the max number of different ports based of the maximum supported
 * (spark jobs / execution groups) that can run at the same time.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiCalculatorJmxPortPool {

    /**
     * Method to return the JMX port mapping.
     *
     * @return a {@link String} port number for the spark executor to use
     */
    public static Queue<String> createJmxPortMapping(final int sparkExecutorStartingPort, final int maximumConcurrentCalculations) {
        return IntStream.range(0, maximumConcurrentCalculations)
                .mapToObj(nextAvailablePort -> String.valueOf(sparkExecutorStartingPort + nextAvailablePort))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
