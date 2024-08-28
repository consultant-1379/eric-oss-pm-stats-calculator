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

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalDateTimes {

    public static LocalDateTime min(@NonNull final LocalDateTime left, final LocalDateTime right) {
        return left.isBefore(right) ? left : right;
    }

    public static LocalDateTime max(@NonNull final LocalDateTime left, final LocalDateTime right) {
        return left.isBefore(right) ? right : left;
    }

}
