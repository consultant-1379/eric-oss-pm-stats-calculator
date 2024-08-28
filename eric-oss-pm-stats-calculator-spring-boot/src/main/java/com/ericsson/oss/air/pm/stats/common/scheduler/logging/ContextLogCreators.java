/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.scheduler.logging;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.quartz.JobDataMap;

@NoArgsConstructor(access = PRIVATE)
public final class ContextLogCreators {

    public static String createContextLog(@NonNull final JobDataMap context) {
        final StringBuilder stringBuilder = new StringBuilder("Values:");
        stringBuilder.append(System.lineSeparator());

        context.forEach((key, value) -> stringBuilder.append('\t').append(key).append(" = ").append(value).append(System.lineSeparator()));

        return stringBuilder.toString();
    }

}
