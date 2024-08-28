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

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import java.text.ParseException;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.quartz.CronExpression;

class CronScheduleTest {
    private static final String NAME = "name";
    private static final String EXPRESSION = "expression";

    @Test
    void shouldCreateCronSchedule_withStaticConstructor() throws Exception {
        try (final MockedStatic<CronExpression> cronExpressionMockedStatic = mockStatic(CronExpression.class)) {
            final Verification verification = () -> CronExpression.validateExpression(EXPRESSION);
            cronExpressionMockedStatic.when(verification).thenAnswer(invocation -> null);

            final CronSchedule actual = CronSchedule.of(NAME, EXPRESSION);

            cronExpressionMockedStatic.verify(verification);

            Assertions.assertThat(actual).isNotNull();
        }
    }

    @Test
    void shouldCreateCronSchedule() throws Exception {
        try (final MockedStatic<CronExpression> cronExpressionMockedStatic = mockStatic(CronExpression.class)) {
            final Verification verification = () -> CronExpression.validateExpression(EXPRESSION);
            cronExpressionMockedStatic.when(verification).thenAnswer(invocation -> null);

            final CronSchedule actual = new CronSchedule(NAME, Collections.emptyMap(), EXPRESSION);

            cronExpressionMockedStatic.verify(verification);

            Assertions.assertThat(actual).isNotNull();
        }
    }

    @Test
    void shouldCreateCronSchedule_whenExpressionIsNever() throws Exception {
        try (final MockedStatic<CronExpression> cronExpressionMockedStatic = mockStatic(CronExpression.class)) {
            final String expression = DefinedCronSchedule.NEVER.getCronSchedule();
            final Verification verification = () -> CronExpression.validateExpression(expression);
            cronExpressionMockedStatic.when(verification).thenAnswer(invocation -> null);

            final CronSchedule actual = new CronSchedule(NAME, Collections.emptyMap(), expression);

            cronExpressionMockedStatic.verify(verification, never());

            Assertions.assertThat(actual).isNotNull();
        }
    }

    @Test
    void shouldThrowActivitySchedulerException_whenExpressionIsInvalid_onCreate() {
        try (final MockedStatic<CronExpression> cronExpressionMockedStatic = mockStatic(CronExpression.class)) {
            final String errorMessage = "invalid";
            final Verification verification = () -> CronExpression.validateExpression(EXPRESSION);
            cronExpressionMockedStatic.when(verification).thenThrow(new ParseException(errorMessage, 10));

            Assertions.assertThatThrownBy(() -> new CronSchedule(NAME, Collections.emptyMap(), EXPRESSION))
                      .hasRootCauseExactlyInstanceOf(ParseException.class)
                      .hasRootCauseMessage(errorMessage)
                      .isInstanceOf(ActivitySchedulerException.class)
                      .hasMessage("CRON expression '%s' is not valid: %s", EXPRESSION, errorMessage);

            cronExpressionMockedStatic.verify(verification);
        }
    }
}