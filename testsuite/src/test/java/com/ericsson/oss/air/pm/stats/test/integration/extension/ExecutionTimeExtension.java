/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.integration.extension;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ExecutionTimeExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private StopWatch stopWatch;

    @Override
    public void beforeTestExecution(final ExtensionContext context) {
        stopWatch = StopWatch.createStarted();
        System.out.printf("[TEST START] Test running: %s.%s %n", context.getRequiredTestClass(), context.getDisplayName());
    }

    @Override
    public void afterTestExecution(final ExtensionContext context) {
        if (!context.getExecutionException().isPresent()) {
            System.out.printf("[TEST ENDED] Took: %s %n", stopWatch);
            System.out.println();
        }
    }
}
