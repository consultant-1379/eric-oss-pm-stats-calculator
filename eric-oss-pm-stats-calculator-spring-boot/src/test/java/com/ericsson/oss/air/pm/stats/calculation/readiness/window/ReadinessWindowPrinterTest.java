/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculation.readiness.window;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculation.readiness.model.ReadinessWindow;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DataSource;
import com.ericsson.oss.air.pm.stats.calculation.readiness.model.domain.DefinitionName;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class ReadinessWindowPrinterTest {
    static final LocalDateTime TEST_TIME = LocalDateTime.of(2_022, Month.OCTOBER, 8, 12, 0);

    ReadinessWindowPrinter objectUnderTest = new ReadinessWindowPrinter();

    @Test
    void shouldPrintReadinessWindows(final CapturedOutput capturedOutput) {
        objectUnderTest.logReadinessWindows("executionGroup", Map.of(
                DefinitionName.of("Y"),
                List.of(
                        ReadinessWindow.of(DataSource.of("support_unit"), TEST_TIME.plusMinutes(10), TEST_TIME.plusMinutes(20)),
                        ReadinessWindow.of(DataSource.of("cell"), TEST_TIME, TEST_TIME.plusMinutes(5))
                ),
                DefinitionName.of("W"),
                List.of(
                        ReadinessWindow.of(DataSource.of("support_unit"), TEST_TIME.plusMinutes(10), TEST_TIME.plusMinutes(20)),
                        ReadinessWindow.of(DataSource.of("cell_unit"), TEST_TIME.plusMinutes(15), TEST_TIME.plusMinutes(20)),
                        ReadinessWindow.of(DataSource.of("cell"), TEST_TIME, TEST_TIME.plusMinutes(5))
                )
        ));

        // As we have json loggers, whitespaces became somewhat unpredictable, hence only the content is important,
        // sometimes, this unit test fails on the pipeline, thus, changing the assertion.
        Assertions.assertThat(capturedOutput.getAll())
                .contains("Readiness windows: 'executionGroup'")
                .contains("Definition 'W' depends on:")
                .contains("Datasource 'cell' has window between [2022-10-08T12:00 - 2022-10-08T12:05]")
                .contains("Datasource 'cell_unit' has window between [2022-10-08T12:15 - 2022-10-08T12:20]")
                .contains("Datasource 'support_unit' has window between [2022-10-08T12:10 - 2022-10-08T12:20]")
                .contains("Definition 'Y' depends on:")
                .contains("Datasource 'cell' has window between [2022-10-08T12:00 - 2022-10-08T12:05]")
                .contains("Datasource 'support_unit' has window between [2022-10-08T12:10 - 2022-10-08T12:20]");
    }
}
