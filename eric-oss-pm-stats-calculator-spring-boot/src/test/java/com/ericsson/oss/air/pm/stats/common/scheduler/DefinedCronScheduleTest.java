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

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class DefinedCronScheduleTest {

    @ParameterizedTest
    @ArgumentsSource(DefinedCronScheduleArgumentProvider.class)
    void shouldVerifyDefinedCronScheduleMetadataRemainsTheSame(final DefinedCronSchedule definedCronSchedule, final String cronSchedule) {
        Assertions.assertThat(definedCronSchedule.getCronSchedule()).isEqualTo(cronSchedule);
    }

    @Test
    void shouldTestAllEnum() {
        Assertions.assertThat(DefinedCronSchedule.values())
                  .containsExactlyInAnyOrder(DefinedCronSchedule.IMMEDIATE,
                                             DefinedCronSchedule.NEVER,
                                             DefinedCronSchedule.EVERY_FIFTEEN_MINUTES,
                                             DefinedCronSchedule.EVERY_HOUR,
                                             DefinedCronSchedule.EVERY_DAY);
    }

    private static final class DefinedCronScheduleArgumentProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(Arguments.of(DefinedCronSchedule.IMMEDIATE, StringUtils.EMPTY),
                             Arguments.of(DefinedCronSchedule.NEVER, "NEVER"),
                             Arguments.of(DefinedCronSchedule.EVERY_FIFTEEN_MINUTES, "0 0/15 * 1/1 * ? *"),
                             Arguments.of(DefinedCronSchedule.EVERY_HOUR, "0 * 0/1 1/1 * ? *"),
                             Arguments.of(DefinedCronSchedule.EVERY_DAY, "0 0 0 1/1 * ? * ?"));
        }
    }
}