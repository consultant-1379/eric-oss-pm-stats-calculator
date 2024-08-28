/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex.table;

import static kpi.model._helper.MotherObject.kpiDefinition;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.model.attribute.Attribute;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Mapper;
import kpi.model._helper.Serialization;
import kpi.model._helper._assert.DefinitionAssertions;
import kpi.model.api.AttributeContract;
import kpi.model.api.element.ElementBase;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.ComplexKpiDefinitions;
import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.complex.ComplexTable;
import kpi.model.complex.ComplexTable.ComplexTableBuilder;
import kpi.model.complex.table.optional.ComplexTableAggregationPeriod;
import kpi.model.complex.table.optional.ComplexTableDataLookBackLimit;
import kpi.model.complex.table.optional.ComplexTableDataReliabilityOffset;
import kpi.model.complex.table.optional.ComplexTableExportable;
import kpi.model.complex.table.optional.ComplexTableReexportLateData;
import kpi.model.complex.table.required.ComplexTableAggregationElements;
import kpi.model.complex.table.required.ComplexTableAlias;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ComplexKpiDefinitionsTest implements AttributeContract<List<ComplexKpiDefinition>> {
    @Override
    public Attribute<List<ComplexKpiDefinition>> createInstance() {
        return ComplexKpiDefinitions.of(List.of(kpiDefinition(
                "dummy_definition",
                "FROM dummy_expression",
                "INTEGER",
                AggregationType.SUM,
                "COMPLEX1",
                Collections.singletonList("table.column"),
                false,
                Collections.singletonList("dummy_filter"),
                30,
                45,
                false
        )));
    }

    @Override
    @Disabled("Not verifying toString for ComplexKpiDefinitions due to its length")
    public void shouldValidateRepresentation() {
    }

    @Override
    public String name() {
        return "kpi_definitions";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public String representation() {
        throw new UnsupportedOperationException();
    }

    @Nested
    class Deserialization {
        @Nested
        class Success {
            @Test
            void whenOnlyRequiredFieldsArePresent_andInheritanceIsApplied() {
                final String content = "{ " +
                        "    \"kpi_definitions\": [" +
                        "        { " +
                        "            \"name\": \"dummy_definition\", " +
                        "            \"expression\": \"FROM dummy_expression\", " +
                        "            \"object_type\": \"INTEGER\", " +
                        "            \"aggregation_type\": \"SUM\", " +
                        "            \"execution_group\": \"COMPLEX1\" " +
                        "        } " +
                        "    ]" +
                        "} ";
                final ComplexKpiDefinitions attribute = Serialization.deserialize(content, ComplexKpiDefinitions.class);
                final ComplexKpiDefinition kpiDefinitionWhenRequiredFieldsArePresent = kpiDefinition(
                        "dummy_definition",
                        "FROM dummy_expression",
                        "INTEGER",
                        AggregationType.SUM,
                        "COMPLEX1",
                        Collections.emptyList(),
                        null,
                        Collections.emptyList(),
                        null,
                        null,
                        null
                );

                /* Optional values are instantiated to nulls or [] since method is used to set those values */
                Assertions.assertThat(attribute).first().satisfies(complexKpiDefinition -> {
                    DefinitionAssertions.assertThat(complexKpiDefinition).isEqualTo(kpiDefinitionWhenRequiredFieldsArePresent);
                });

                final ComplexTableAggregationElements tableAggregationElements = ComplexTableAggregationElements.of(Mapper.toComplexAggregationElements("table.column"));
                final ComplexTableAlias tableAlias = ComplexTableAlias.of("dummy_alias");
                final ComplexTableAggregationPeriod complexTableAggregationPeriod = ComplexTableAggregationPeriod.of(60);
                final ComplexTableExportable tableIsExported = ComplexTableExportable.of(true);
                final ComplexTableDataLookBackLimit tableDataLookBackLimit = ComplexTableDataLookBackLimit.of(45);
                final ComplexTableDataReliabilityOffset tableDataReliabilityOffset = ComplexTableDataReliabilityOffset.of(30);
                final ComplexTableReexportLateData tableReexportLateData = ComplexTableReexportLateData.of(true);

                final ComplexTableBuilder complexTableBuilder = ComplexTable.builder();
                complexTableBuilder.alias(tableAlias);
                complexTableBuilder.aggregationPeriod(complexTableAggregationPeriod);
                complexTableBuilder.aggregationElements(tableAggregationElements);
                complexTableBuilder.exportable(tableIsExported);
                complexTableBuilder.dataReliabilityOffset(tableDataReliabilityOffset);
                complexTableBuilder.dataLookBackLimit(tableDataLookBackLimit);
                complexTableBuilder.reexportLateData(tableReexportLateData);
                complexTableBuilder.kpiDefinitions(ComplexKpiDefinitions.of(List.of(kpiDefinitionWhenRequiredFieldsArePresent)));

                attribute.withInheritedValuesFrom(complexTableBuilder.build());

                Assertions.assertThat(attribute).first().satisfies(complexKpiDefinition -> {
                    DefinitionAssertions.assertThat(complexKpiDefinition).isEqualTo(
                            kpiDefinition(
                                    "dummy_definition",
                                    "FROM dummy_expression",
                                    "INTEGER",
                                    AggregationType.SUM,
                                    "COMPLEX1",
                                    tableAggregationElements.value().stream().map(ElementBase::value).collect(Collectors.toList()),
                                    tableIsExported.value(),
                                    Collections.emptyList(),
                                    tableDataReliabilityOffset.value(),
                                    tableDataLookBackLimit.value(),
                                    tableReexportLateData.value()
                            )
                    );
                });
            }

            @Test
            void whenAllFieldsArePresent() {
                final String content = "{ " +
                        "    \"kpi_definitions\": [" +
                        "        { " +
                        "            \"name\": \"dummy_definition\", " +
                        "            \"expression\": \"FROM dummy_expression\", " +
                        "            \"object_type\": \"INTEGER\", " +
                        "            \"aggregation_type\": \"SUM\", " +
                        "            \"execution_group\": \"COMPLEX1\", " +
                        "            \"aggregation_elements\": [\"table.column\"], " +
                        "            \"exportable\": false, " +
                        "            \"filters\": [\"dummy_filter\"], " +
                        "            \"data_reliability_offset\": 30, " +
                        "            \"data_lookback_limit\": 45, " +
                        "            \"reexport_late_data\": false " +
                        "        } " +
                        "    ]" +
                        "} ";
                final ComplexKpiDefinitions attribute = Serialization.deserialize(content, ComplexKpiDefinitions.class);

                Assertions.assertThat(attribute).first().satisfies(complexKpiDefinition -> {
                    DefinitionAssertions.assertThat(complexKpiDefinition).isEqualTo(
                            kpiDefinition(
                                    "dummy_definition",
                                    "FROM dummy_expression",
                                    "INTEGER",
                                    AggregationType.SUM,
                                    "COMPLEX1",
                                    Collections.singletonList("table.column"),
                                    false,
                                    Collections.singletonList("dummy_filter"),
                                    30,
                                    45,
                                    false
                            )
                    );
                });
            }
        }

        @Nested
        class Failure {
            @Test
            void whenPostAggregationDataSourceAndFiltersAreGiven() {
                final String content = "{ " +
                        "    \"kpi_definitions\": [" +
                        "        { " +
                        "            \"name\": \"dummy_definition\", " +
                        "            \"expression\": \"FROM kpi_post_agg://\", " +
                        "            \"object_type\": \"INTEGER\", " +
                        "            \"aggregation_type\": \"SUM\", " +
                        "            \"execution_group\": \"COMPLEX1\", " +
                        "            \"filters\": [\"dummy_filter\"] " +
                        "        } " +
                        "    ]" +
                        "} ";
                Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, ComplexKpiDefinitions.class))
                        .isInstanceOf(ValueInstantiationException.class)
                        .hasRootCauseInstanceOf(IllegalArgumentException.class)
                        .getRootCause()
                        .hasMessage("'filters' attribute must be empty when 'expression' attribute contains 'kpi_post_agg://' or 'kpi_inmemory://'");
            }

            @Test
            void whenInMemoryDataSourceAndFiltersAreGiven() {
                final String content = "{ " +
                        "    \"kpi_definitions\": [" +
                        "        { " +
                        "            \"name\": \"dummy_definition\", " +
                        "            \"expression\": \"FROM kpi_inmemory://\", " +
                        "            \"object_type\": \"INTEGER\", " +
                        "            \"aggregation_type\": \"SUM\", " +
                        "            \"execution_group\": \"COMPLEX1\", " +
                        "            \"filters\": [\"dummy_filter\"] " +
                        "        } " +
                        "    ]" +
                        "} ";
                Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, ComplexKpiDefinitions.class))
                        .isInstanceOf(ValueInstantiationException.class)
                        .hasRootCauseInstanceOf(IllegalArgumentException.class)
                        .getRootCause()
                        .hasMessage("'filters' attribute must be empty when 'expression' attribute contains 'kpi_post_agg://' or 'kpi_inmemory://'");
            }

            @Test
            void whenKpiDefinitionIsNotPresent() {
                final String content = "{ " +
                        "    \"kpi_definitions\": []" +
                        "} ";
                Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, ComplexKpiDefinitions.class))
                        .isInstanceOf(ValueInstantiationException.class)
                        .hasRootCauseInstanceOf(IllegalArgumentException.class)
                        .getRootCause()
                        .hasMessageContaining("At least one kpi definition is required");
            }
        }
    }
}
