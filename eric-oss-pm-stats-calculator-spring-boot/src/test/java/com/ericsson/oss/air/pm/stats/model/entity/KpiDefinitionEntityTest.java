/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.entity;

import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import static com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.builder;

import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.KpiDefinitionPatchRequest;

import lombok.NonNull;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;

class KpiDefinitionEntityTest {

    @Nested
    class KpiTypeVerification {
        @Test
        void shouldVerifyComplex() {
            final KpiDefinitionEntityBuilder builder = builder();
            builder.withAlias("alias");
            builder.withAggregationPeriod(-1);
            builder.withExecutionGroup(ExecutionGroup.builder().withId(1).withName("complex").build());
            final KpiDefinitionEntity entity = builder.build();

            final KpiType actual = entity.kpiType();
            Assertions.assertThat(actual).isEqualTo(KpiType.SCHEDULED_COMPLEX);
        }
    }

    @Nested
    class ReturnTableName {
        @Test
        void forDefaultAggregationPeriod() {
            final KpiDefinitionEntity entity = builder().withAlias("alias").withAggregationPeriod(-1).build();
            final String actual = entity.tableName();
            Assertions.assertThat(actual).isEqualTo("kpi_alias_");
        }

        @Test
        void forNonDefaultAggregationPeriod() {
            final KpiDefinitionEntity entity = builder().withAlias("alias").withAggregationPeriod(60).build();
            final String actual = entity.tableName();
            Assertions.assertThat(actual).isEqualTo("kpi_alias_60");
        }
    }

    @Nested
    class ReturnDataIdentifier {

        @Test
        void whenEveryPropertyIsReady_thenDataIdentifierReturnsCorrectValue() {
            final KpiDefinitionEntity entity = entity("dataSpace", "category", "schema");
            final DataIdentifier actual = entity.dataIdentifier();
            Assertions.assertThat(actual).isEqualTo(DataIdentifier.of("dataSpace|category|schema"));
        }

        @CsvSource({
                ",category,schema",
                "dataSpace,,schema",
                "dataSpace,category,"
        })
        @ParameterizedTest(name = "[{index}] Data Space: ''{0}'' Category: ''{1}'' Schema: ''{2}''")
        void shouldVerifyNullCase(@NonNull final ArgumentsAccessor arguments) {
            final String dataSpace = arguments.getString(0);
            final String category = arguments.getString(1);
            final String schemaName = arguments.getString(2);
            final KpiDefinitionEntity entity = entity(dataSpace, category, schemaName);
            final DataIdentifier actual = entity.dataIdentifier();
            Assertions.assertThat(actual).isNull();
        }

        KpiDefinitionEntity entity(final String dataSpace, final String category, final String schemaName) {
            final KpiDefinitionEntityBuilder builder = builder();
            builder.withSchemaDataSpace(dataSpace);
            builder.withSchemaCategory(category);
            builder.withSchemaName(schemaName);
            return builder.build();
        }
    }

    @Nested
    class Update {
        @Test
        void shouldUpdate() {
            final KpiDefinitionEntity actual = builder()
                    .withName("kpi")
                    .withAlias("alias")
                    .withExpression("expression")
                    .withObjectType("objectType")
                    .withAggregationPeriod(60)
                    .withAggregationElements(List.of("agg_element"))
                    .withFilters(List.of("filter_1", "filter_2"))
                    .withDataReliabilityOffset(1)
                    .withDataLookbackLimit(1)
                    .withReexportLateData(true)
                    .withExportable(true)
                    .build();

            final KpiDefinitionPatchRequest request = KpiDefinitionPatchRequest.builder()
                    .expression("expression_2")
                    .objectType("objectType_2")
                    .filters(List.of("filter_3", "filter_4"))
                    .dataLookbackLimit(2)
                    .reexportLateData(false)
                    .exportable(false)
                    .build();

            actual.update(request);

            Assertions.assertThat(actual).satisfies(entity -> {
                Assertions.assertThat(entity.name()).isEqualTo("kpi");
                Assertions.assertThat(entity.alias()).isEqualTo("alias");
                Assertions.assertThat(entity.expression()).isEqualTo("expression_2");
                Assertions.assertThat(entity.objectType()).isEqualTo("objectType_2");
                Assertions.assertThat(entity.aggregationPeriod()).isEqualTo(60);
                Assertions.assertThat(entity.aggregationElements()).containsExactly("agg_element");
                Assertions.assertThat(entity.dataIdentifier()).isNull();
                Assertions.assertThat(entity.executionGroup()).isNull();
                Assertions.assertThat(entity.exportable()).isFalse();
                Assertions.assertThat(entity.dataReliabilityOffset()).isOne();
                Assertions.assertThat(entity.dataLookbackLimit()).isEqualTo(2);
                Assertions.assertThat(entity.filters()).containsExactlyInAnyOrder("filter_3", "filter_4");
                Assertions.assertThat(entity.reexportLateData()).isFalse();
                Assertions.assertThat(entity.schemaDetail()).isNull();
            });
        }
    }
}