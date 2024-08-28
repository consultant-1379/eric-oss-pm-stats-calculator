/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler;

/**
 * Exception class used in the {@code ActivityScheduler}. Client can provide a boolean to tell the {@code ActivityScheduler} whether or not to refire
 * the {@link Activity} when there is an error.
 */
public class ActivityException extends Exception {

    private static final long serialVersionUID = 3_353_606_991_796_555_650L;

    private final boolean refire;

    public ActivityException(final String message) {
        super(message);
        refire = false;
    }

    /**
     * Should the {@link Activity} that threw this {@link ActivityException} be re-fired or not.
     *
     * @return true if the {@link Activity} should be re-fired
     */
    public boolean isRefire() {
        return refire;
    }
}