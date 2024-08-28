/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.configuration.property.converter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationPropertiesBinding
public class StringToDurationConverter implements Converter<String, Duration> {
    public static final Duration ALL = ChronoUnit.FOREVER.getDuration();

    @Override
    public Duration convert(@NonNull final String source) {
        return "ALL".equalsIgnoreCase(source)
                ? ALL
                : DurationStyle.detectAndParse(source);
    }
}
