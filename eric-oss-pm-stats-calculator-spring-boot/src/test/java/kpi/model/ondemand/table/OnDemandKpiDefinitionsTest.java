/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.ondemand.table;

import static kpi.model._helper.Mapper.toOnDemandAggregationElements;
import static kpi.model._helper.MotherObject.kpiDefinition;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.model.attribute.Attribute;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model._helper._assert.DefinitionAssertions;
import kpi.model.api.AttributeContract;
import kpi.model.api.element.ElementBase;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.api.table.definition.OnDemandKpiDefinitions;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.OnDemandTable.OnDemandTableBuilder;
import kpi.model.ondemand.table.optional.OnDemandTableExportable;
import kpi.model.ondemand.table.required.OnDemandTableAggregationElements;
import kpi.model.ondemand.table.required.OnDemandTableAlias;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OnDemandKpiDefinitionsTest implements AttributeContract<List<OnDemandKpiDefinition>> {
    @Override
    public Attribute<List<OnDemandKpiDefinition>> createInstance() {
        return OnDemandKpiDefinitions.of(List.of(kpiDefinition(
                "dummy_definition",
                "FROM dummy_expression",
                "INTEGER",
                AggregationType.SUM,
                Collections.singletonList("table.column"),
                false,
                Collections.singletonList("dummy_filter")
        )));
    }

    @Override
    @Disabled("Not verifying toString for OnDemandKpiDefinitions due to its length")
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
                        "            \"aggregation_type\": \"SUM\" " +
                        "        } " +
                        "    ]" +
                        "} ";
                final OnDemandKpiDefinitions attribute = Serialization.deserialize(content, OnDemandKpiDefinitions.class);
                final OnDemandKpiDefinition kpiDefinitionWhenRequiredFieldsArePresent = kpiDefinition(
                        "dummy_definition",
                        "FROM dummy_expression",
                        "INTEGER",
                        AggregationType.SUM,
                        Collections.emptyList(),
                        null,
                        Collections.emptyList()

                );

                /* Optional values are instantiated to nulls or [] since method is used to set those values */
                Assertions.assertThat(attribute).first().satisfies(onDemandKpiDefinition -> {
                    DefinitionAssertions.assertThat(onDemandKpiDefinition).isEqualTo(kpiDefinitionWhenRequiredFieldsArePresent);
                });

                final OnDemandTableAggregationElements tableAggregationElements = OnDemandTableAggregationElements.of(toOnDemandAggregationElements("table.column"));
                final OnDemandTableExportable tableExportable = OnDemandTableExportable.of(true);
                final OnDemandTableAlias tableAlias = OnDemandTableAlias.of("dummy_alias");

                final OnDemandTableBuilder ondemandTableBuilder = OnDemandTable.builder();
                ondemandTableBuilder.alias(tableAlias);
                ondemandTableBuilder.aggregationElements(tableAggregationElements);
                ondemandTableBuilder.exportable(tableExportable);
                ondemandTableBuilder.kpiDefinitions(OnDemandKpiDefinitions.of(List.of(kpiDefinitionWhenRequiredFieldsArePresent)));

                attribute.withInheritedValuesFrom(ondemandTableBuilder.build());

                Assertions.assertThat(attribute).first().satisfies(onDemandKpiDefinition -> {
                    DefinitionAssertions.assertThat(onDemandKpiDefinition).isEqualTo(
                            kpiDefinition(
                                    "dummy_definition",
                                    "FROM dummy_expression",
                                    "INTEGER",
                                    AggregationType.SUM,
                                    tableAggregationElements.value().stream().map(ElementBase::value).collect(Collectors.toList()),
                                    true,
                                    Collections.emptyList()
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
                        "            \"aggregation_elements\": [\"table.column\"], " +
                        "            \"exportable\": false, " +
                        "            \"filters\": [\"dummy_filter\"], " +
                        "            \"reexport_late_data\": false " +
                        "        } " +
                        "    ]" +
                        "} ";
                final OnDemandKpiDefinitions attribute = Serialization.deserialize(content, OnDemandKpiDefinitions.class);

                Assertions.assertThat(attribute).first().satisfies(onDemandKpiDefinition -> {
                    DefinitionAssertions.assertThat(onDemandKpiDefinition).isEqualTo(
                            kpiDefinition(
                                    "dummy_definition",
                                    "FROM dummy_expression",
                                    "INTEGER",
                                    AggregationType.SUM,
                                    Collections.singletonList("table.column"),
                                    false,
                                    Collections.singletonList("dummy_filter")

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
                Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, OnDemandKpiDefinitions.class))
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
                Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, OnDemandKpiDefinitions.class))
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
                Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, OnDemandKpiDefinitions.class))
                        .isInstanceOf(ValueInstantiationException.class)
                        .hasRootCauseInstanceOf(IllegalArgumentException.class)
                        .getRootCause()
                        .hasMessageContaining("At least one kpi definition is required");
            }

        }
    }
}