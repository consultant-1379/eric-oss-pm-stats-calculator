/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple.table;

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
import kpi.model.api.table.definition.SimpleKpiDefinitions;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import kpi.model.simple.SimpleTable;
import kpi.model.simple.SimpleTable.SimpleTableBuilder;
import kpi.model.simple.table.optional.SimpleTableAggregationPeriod;
import kpi.model.simple.table.optional.SimpleTableDataLookBackLimit;
import kpi.model.simple.table.optional.SimpleTableDataReliabilityOffset;
import kpi.model.simple.table.optional.SimpleTableExportable;
import kpi.model.simple.table.optional.SimpleTableReexportLateData;
import kpi.model.simple.table.required.SimpleTableAggregationElements;
import kpi.model.simple.table.required.SimpleTableAlias;
import kpi.model.simple.table.required.SimpleTableInpDataIdentifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SimpleKpiDefinitionsTest implements AttributeContract<List<SimpleKpiDefinition>> {
    @Override
    public Attribute<List<SimpleKpiDefinition>> createInstance() {
        return SimpleKpiDefinitions.of(List.of(SimpleKpiDefinition.builder().build()));
    }

    @Override
    @Disabled("Not verifying toString for SimpleKpiDefinitions due to its length")
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
                        "            \"expression\": \"dummy_expression\", " +
                        "            \"object_type\": \"INTEGER\", " +
                        "            \"aggregation_type\": \"SUM\" " +
                        "        } " +
                        "    ]" +
                        "} ";
                final SimpleKpiDefinitions attribute = Serialization.deserialize(content, SimpleKpiDefinitions.class);
                final SimpleKpiDefinition kpiDefinitionWhenRequiredFieldsArePresent = kpiDefinition(
                        "dummy_definition",
                        "dummy_expression",
                        "INTEGER",
                        AggregationType.SUM,
                        Collections.emptyList(),
                        null,
                        Collections.emptyList(),
                        null,
                        null,
                        null,
                        null
                );

                /* Optional values are instantiated to nulls or [] since method is used to set those values */
                Assertions.assertThat(attribute).first().satisfies(simpleKpiDefinition -> {
                    DefinitionAssertions.assertThat(simpleKpiDefinition).isEqualTo(kpiDefinitionWhenRequiredFieldsArePresent);
                });

                final SimpleTableAggregationElements tableAggregationElements = SimpleTableAggregationElements.of(Mapper.toAggregationElements("table.column"));
                final SimpleTableExportable tableIsExported = SimpleTableExportable.of(true);
                final SimpleTableAlias tableAlias = SimpleTableAlias.of("dummy_alias");
                final SimpleTableAggregationPeriod tableAggregationPeriod = SimpleTableAggregationPeriod.of(60);
                final SimpleTableDataLookBackLimit tableDataLookBackLimit = SimpleTableDataLookBackLimit.of(45);
                final SimpleTableDataReliabilityOffset tableDataReliabilityOffset = SimpleTableDataReliabilityOffset.of(30);
                final SimpleTableReexportLateData tableReexportLateData = SimpleTableReexportLateData.of(true);
                final SimpleTableInpDataIdentifier tableInpDataIdentifier = SimpleTableInpDataIdentifier.of("dataSpace|category|schema");

                final SimpleTableBuilder simpleTableBuilder = SimpleTable.builder();
                simpleTableBuilder.alias(tableAlias);
                simpleTableBuilder.aggregationPeriod(tableAggregationPeriod);
                simpleTableBuilder.aggregationElements(tableAggregationElements);
                simpleTableBuilder.exportable(tableIsExported);
                simpleTableBuilder.dataReliabilityOffset(tableDataReliabilityOffset);
                simpleTableBuilder.dataLookBackLimit(tableDataLookBackLimit);
                simpleTableBuilder.reexportLateData(tableReexportLateData);
                simpleTableBuilder.inpDataIdentifier(tableInpDataIdentifier);
                simpleTableBuilder.kpiDefinitions(SimpleKpiDefinitions.of(List.of(kpiDefinitionWhenRequiredFieldsArePresent)));

                attribute.withInheritedValuesFrom(simpleTableBuilder.build());

                Assertions.assertThat(attribute).first().satisfies(simpleKpiDefinition -> {
                    DefinitionAssertions.assertThat(simpleKpiDefinition).isEqualTo(
                            kpiDefinition(
                                    "dummy_definition",
                                    "dummy_expression",
                                    "INTEGER",
                                    AggregationType.SUM,
                                    tableAggregationElements.value().stream().map(ElementBase::value).collect(Collectors.toList()),
                                    tableIsExported.value(),
                                    Collections.emptyList(),
                                    tableDataReliabilityOffset.value(),
                                    tableDataLookBackLimit.value(),
                                    tableReexportLateData.value(),
                                    tableInpDataIdentifier.value()
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
                        "            \"expression\": \"dummy_expression\", " +
                        "            \"object_type\": \"INTEGER\", " +
                        "            \"aggregation_type\": \"SUM\", " +
                        "            \"aggregation_elements\": [\"table.column\"], " +
                        "            \"exportable\": false, " +
                        "            \"filters\": [\"dummy_filter\"], " +
                        "            \"data_reliability_offset\": 30, " +
                        "            \"data_lookback_limit\": 45, " +
                        "            \"reexport_late_data\": false, " +
                        "            \"inp_data_identifier\": \"dataSpace|category|schema\" " +
                        "        } " +
                        "    ]" +
                        "} ";
                final SimpleKpiDefinitions attribute = Serialization.deserialize(content, SimpleKpiDefinitions.class);

                Assertions.assertThat(attribute).first().satisfies(simpleKpiDefinition -> {
                    DefinitionAssertions.assertThat(simpleKpiDefinition).isEqualTo(
                            kpiDefinition(
                                    "dummy_definition",
                                    "dummy_expression",
                                    "INTEGER",
                                    AggregationType.SUM,
                                    Collections.singletonList("table.column"),
                                    false,
                                    Collections.singletonList("dummy_filter"),
                                    30,
                                    45,
                                    false,
                                    "dataSpace|category|schema"
                            )
                    );
                });
            }
        }

        @Nested
        class Failure {
            @Test
            void whenKpiDefinitionIsNotPresent() {
                final String content = "{ " +
                        "    \"kpi_definitions\": []" +
                        "} ";
                Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, SimpleKpiDefinitions.class))
                        .isInstanceOf(ValueInstantiationException.class)
                        .hasRootCauseInstanceOf(IllegalArgumentException.class)
                        .getRootCause()
                        .hasMessageContaining("At least one kpi definition is required");
            }
        }
    }
}