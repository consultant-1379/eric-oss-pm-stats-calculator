/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.integration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ericsson.oss.air.pm.stats.test.integration.extension.ExecutionTimeExtension;
import com.ericsson.oss.air.pm.stats.test.integration.extension.FailFastExtension;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;


/**
 * Annotation combining all {@link Extension}s required to run integration tests.
 * <br>
 * <strong>NOTE</strong>: Integration tests should always be annotated with this annotation.
 * @see ExecutionTimeExtension
 * @see FailFastExtension
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestMethodOrder(OrderAnnotation.class)
@ExtendWith({
        ExecutionTimeExtension.class,
        FailFastExtension.class
})
public @interface IntegrationTest {
}
