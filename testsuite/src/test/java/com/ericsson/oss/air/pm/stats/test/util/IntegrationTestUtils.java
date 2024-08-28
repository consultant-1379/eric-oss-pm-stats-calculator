/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class with methods for integration tests.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntegrationTestUtils {

    /**
     * Sleeps for the provided amount of time.
     * <p>
     * Not ideal to use in integration tests, as it can be very environment dependent, but until all services expose some way to poll status, if we
     * have tests that consume/process events, we need to wait for them to complete.
     *
     * @param timeToSleep
     *            the time to sleep
     * @param timeUnit
     *            the time unit of the time provided
     */
    public static void sleep(final long timeToSleep, final TimeUnit timeUnit) {
        try {
            if(log.isInfoEnabled()) {
                log.info("Sleeping for {} {}...", timeToSleep, timeUnit.toString().toLowerCase(Locale.UK));
            }
            timeUnit.sleep(timeToSleep);
        } catch (final InterruptedException e) {
            log.debug("Error sleeping", e);
        }
    }
}
