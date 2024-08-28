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

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

@Slf4j
public class FailFastExtension implements ExecutionCondition, TestExecutionExceptionHandler {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        final Store store = getStore(context);
        final String value = store.get(FailFastExtension.class, String.class);

        return Optional.ofNullable(value)
                       .map(failure -> ConditionEvaluationResult.disabled(String.format("Test disabled due to previous failure in '%s'", failure)))
                       .orElseGet(() -> ConditionEvaluationResult.enabled("No test failures in fail fast tests"));
    }

    @Override
    public void handleTestExecutionException(final ExtensionContext context, final Throwable throwable) throws Throwable {
        final Store store = getStore(context);

        store.put(FailFastExtension.class, context.getDisplayName());

        log.error("Failure:", throwable);
        throw throwable;
    }

    private static Store getStore(final ExtensionContext context) {
        return storeFor(context, namespaceFor(context));
    }

    private static Namespace namespaceFor(final ExtensionContext extensionContext){
        return Namespace.create(FailFastExtension.class, extensionContext.getParent());
    }


    private static Store storeFor(final ExtensionContext context, final Namespace namespace){
        return context.getParent()
                      .orElseThrow(() -> new IllegalStateException("namespace"))
                      .getStore(namespace);
    }
}
