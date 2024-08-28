/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.function.registry;

import org.apache.spark.sql.catalyst.analysis.SimpleFunctionRegistry;
import org.apache.spark.sql.catalyst.analysis.SimpleFunctionRegistryProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FunctionRegistryProviderTest {

    @Test
    void shouldProvideSimpleFunctionRegistry() {
        final SimpleFunctionRegistry actual = SimpleFunctionRegistryProvider.simpleFunctionRegistry();
        Assertions.assertThat(actual).isNotNull();
    }
}