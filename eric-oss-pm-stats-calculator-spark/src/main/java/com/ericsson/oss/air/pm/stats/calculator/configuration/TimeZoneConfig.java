/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration;

import java.util.TimeZone;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeZoneConfig {

    public static void configTimeZone() {
        log.info("Server using '{}' time zone", TimeZone.getDefault().getDisplayName());
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("Application using '{}' time zone", TimeZone.getDefault().getDisplayName());
    }
}
