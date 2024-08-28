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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ENUM defining some default CRON schedules.
 */
@Getter
@RequiredArgsConstructor
public enum DefinedCronSchedule {
    IMMEDIATE(""),
    NEVER("NEVER"),
    EVERY_FIFTEEN_MINUTES("0 0/15 * 1/1 * ? *"),
    EVERY_HOUR("0 * 0/1 1/1 * ? *"),
    EVERY_DAY("0 0 0 1/1 * ? * ?");

    private final String cronSchedule;
}
