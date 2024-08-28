/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.configuration.rest;

import com.ericsson.oss.air.pm.stats.common.rest.RestExecutor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RestExecutorProducerTest {

    RestExecutorProducer objectUnderTest = new RestExecutorProducer();

    @Test
    void shouldProduceRestExecutor() {
        final RestExecutor actual = objectUnderTest.restExecutor();

        Assertions.assertThat(actual).isNotNull();
    }
}