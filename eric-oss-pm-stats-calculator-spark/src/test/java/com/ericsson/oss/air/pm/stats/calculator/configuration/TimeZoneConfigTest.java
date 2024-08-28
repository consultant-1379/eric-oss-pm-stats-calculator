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

import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class TimeZoneConfigTest {

    @Test
    void setTimeZone() {
        try (final MockedStatic<TimeZone> timeZoneMockedStatic = mockStatic(TimeZone.class, CALLS_REAL_METHODS)) {
            final TimeZone utc = TimeZone.getTimeZone("UTC");

            timeZoneMockedStatic.when(() -> TimeZone.setDefault(utc)).thenAnswer(invocation -> null);

            TimeZoneConfig.configTimeZone();

            timeZoneMockedStatic.verify(() -> TimeZone.setDefault(utc));
        }
    }
}