/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestsuiteRunner {
    public static void main(final String[] args) {

        runTests(KpiCalculationIntTests.class);
        runTests(KpiApiIntTests.class);
        runTests(KpiGraphIntTests.class);
        runTests(SqlReferenceValidationIntTests.class);
        runTests(SqlDatasourceIntTests.class);

        log.info("All tests were completed successfully :)");
        System.exit(0);
    }

    private static PrintWriter standardOutput() {
        return new PrintWriter(System.out);
    }

    private static void runTests(final Class<?>... testClass) {

        final Launcher launcher = LauncherFactory.create();

        final SummaryGeneratingListener summaryGeneratingListener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(summaryGeneratingListener);

        final LauncherDiscoveryRequest request = buildRequest(testClass);
        launcher.execute(request);

        final TestExecutionSummary testSummary = summaryGeneratingListener.getSummary();
        testSummary.printTo(standardOutput());

        if (!testSummary.getFailures().isEmpty()) {
            log.error("One of the tests failed therefore the testing was aborted.");

            testSummary.printFailuresTo(standardOutput());
            System.exit(1);
        }
    }


    private static LauncherDiscoveryRequest buildRequest(final Class<?>... testClasses) {
        final List<ClassSelector> selectors = Arrays.stream(testClasses)
                                                    .map(DiscoverySelectors::selectClass)
                                                    .collect(Collectors.toList());

        return LauncherDiscoveryRequestBuilder.request()
                                              .selectors(selectors)
                                              .build();
    }

}
