/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.sql.Timestamp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Timestamps {
    private static final Timestamp INITIAL_TIMESTAMP = initialTimeStamp();
    private static final int INITIAL_TIME = 0;

    public static Timestamp copy(final Timestamp timestamp) {
        return new Timestamp(timestamp.getTime());
    }

    public static Timestamp initialTimeStamp() {
        return new Timestamp(INITIAL_TIME);
    }

    public static boolean isInitialTimestamp(@NonNull final Timestamp timestamp) {
        return INITIAL_TIMESTAMP.equals(timestamp);
    }

    public static boolean isNotInitialTimestamp(final Timestamp timestamp) {
        return !isInitialTimestamp(timestamp);
    }

}
