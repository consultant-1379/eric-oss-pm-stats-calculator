/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import java.util.List;

import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * This is only a dummy test for coverage as the validation already tested on the API level.
 */
class KpiDefinitionPatchRequestValidatorTest {
    KpiDefinitionRequestValidator objectUnderTest = new KpiDefinitionRequestValidator();

    @Nested
    class ValidateOnDemand {
        @Test
        void shouldPassValidation() {
            final KpiDefinitionEntity entity = entity(
                    "dummy_definition", "FROM new_expression", "FLOAT", "SUM",
                    List.of("table.column"), true,
                    null, null, null, null, null, null,
                    List.of("dummy_filter"), null
            );
            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.validateRequestAttributes(
                    entity));
        }

        @Test
        void shouldFail() {
            /* Only fail test to ensure the validation indeed working */
            final KpiDefinitionEntity entity = entity(
                    "dummy_definition", "FROM new_expression", "RANDOM", "SUM",
                    List.of("table.column"), true,
                    null, null, null, null, null, null,
                    List.of("dummy_filter"), null
            );
            Assertions.assertThatThrownBy(() -> objectUnderTest.validateRequestAttributes(entity))
                    .isInstanceOfAny(IllegalStateException.class)
                    .hasMessage("'RANDOM' is not a valid ObjectType");
        }
    }

    @Nested
    class ValidateComplex {
        @Test
        void shouldPassValidation() {
            final KpiDefinitionEntity entity = entity(
                    "dummy_definition", "FROM new_expression", "FLOAT", "SUM",
                    List.of("table.column"), true,
                    null, null, null, ExecutionGroup.builder().withName("COMPLEX1").build(), 30, 45,
                    List.of("dummy_filter"), true
            );
            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.validateRequestAttributes(
                    entity));
        }
    }

    @Nested
    class ValidateSimple {
        @Test
        void shouldPassValidation() {
            final KpiDefinitionEntity entity = entity(
                    "dummy_definition", "new_expression", "FLOAT", "SUM",
                    List.of("table.column"), true,
                    "space", "category", "schema", null, 30, 45,
                    List.of("dummy_filter"), true
            );
            Assertions.assertThatNoException().isThrownBy(() -> objectUnderTest.validateRequestAttributes(
                    entity));
        }
    }

    static KpiDefinitionEntity entity(
            final String name, final String expression, final String objectType, final String aggregationType,
            final List<String> aggregationElements, final Boolean exportable, final String dataSpace, final String category,
            final String schemaName, final ExecutionGroup executionGroup, final Integer reliabilityOffset,
            final Integer lookbackLimit, final List<String> filters, final Boolean reexportLateData
    ) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withExpression(expression);
        builder.withObjectType(objectType);
        builder.withAggregationType(aggregationType);
        builder.withAggregationElements(aggregationElements);
        builder.withExportable(exportable);
        builder.withSchemaDataSpace(dataSpace);
        builder.withSchemaCategory(category);
        builder.withSchemaName(schemaName);
        builder.withExecutionGroup(executionGroup);
        builder.withDataReliabilityOffset(reliabilityOffset);
        builder.withDataLookbackLimit(lookbackLimit);
        builder.withFilters(filters);
        builder.withReexportLateData(reexportLateData);
        return builder.build();
    }
}
