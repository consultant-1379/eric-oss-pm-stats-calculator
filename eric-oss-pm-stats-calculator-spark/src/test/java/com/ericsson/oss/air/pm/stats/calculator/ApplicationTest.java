/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import static org.apache.commons.lang.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import com.ericsson.oss.air.pm.stats.calculator.configuration.DefaultProperties;
import com.ericsson.oss.air.pm.stats.calculator.configuration.TimeZoneConfig;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.testcontainers.shaded.com.google.common.collect.Iterables;

class ApplicationTest {
    @Test
    void shouldVerifyApplicationStart() {
        try (final MockedStatic<DefaultProperties> hibernateMock = mockStatic(DefaultProperties.class);
             final MockedConstruction<SpringApplication> springApplicationMockedConstruction = mockConstruction(SpringApplication.class);
             final MockedStatic<TimeZoneConfig> timeZoneMock = mockStatic(TimeZoneConfig.class)) {
            final Properties properties = new Properties();
            hibernateMock.when(DefaultProperties::defaultProperties).thenReturn(properties);

            Application.main(EMPTY_STRING_ARRAY);

            final SpringApplication springApplicationMock = Iterables.getOnlyElement(springApplicationMockedConstruction.constructed());

            verify(springApplicationMock).setDefaultProperties(properties);
            verify(springApplicationMock).run(EMPTY_STRING_ARRAY);
            timeZoneMock.verify(TimeZoneConfig::configTimeZone);
            hibernateMock.verify(DefaultProperties::defaultProperties);
        }
    }
}