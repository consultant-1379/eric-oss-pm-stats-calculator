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

import com.ericsson.oss.air.pm.stats.calculator.configuration.DefaultProperties;
import com.ericsson.oss.air.pm.stats.calculator.configuration.TimeZoneConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Application {

    public static void main(final String[] args) {
        TimeZoneConfig.configTimeZone();
        final SpringApplication application = new SpringApplication(Application.class);
        application.setDefaultProperties(DefaultProperties.defaultProperties());
        application.run(args);
    }
}