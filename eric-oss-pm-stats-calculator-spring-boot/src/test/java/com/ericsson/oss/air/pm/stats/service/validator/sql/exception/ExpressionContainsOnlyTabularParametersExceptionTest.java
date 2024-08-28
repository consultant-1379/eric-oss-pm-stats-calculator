/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.exception;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ExpressionContainsOnlyTabularParametersExceptionTest {

    @Test
    void shouldInitializeList() {
        final ExpressionContainsOnlyTabularParametersException actual = new ExpressionContainsOnlyTabularParametersException(Collections.emptyList());

        Assertions.assertThat(actual.getKpiDefinitionsWithOnlyTabularParamRelations()).isEmpty();
        Assertions.assertThat(actual.getName()).isNull();
        Assertions.assertThat(actual.getExpression()).isNull();
    }

    @Test
    void shouldInitializeValues() {
        final ExpressionContainsOnlyTabularParametersException actual = new ExpressionContainsOnlyTabularParametersException("name", "expression");

        Assertions.assertThat(actual.getKpiDefinitionsWithOnlyTabularParamRelations()).isEmpty();
        Assertions.assertThat(actual.getName()).isEqualTo("name");
        Assertions.assertThat(actual.getExpression()).isEqualTo("expression");
    }

}