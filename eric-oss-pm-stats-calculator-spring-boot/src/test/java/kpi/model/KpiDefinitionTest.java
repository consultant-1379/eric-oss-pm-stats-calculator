/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model;

import static kpi.model._helper.Mapper.toAggregationElements;
import static kpi.model._helper.Mapper.toComplexAggregationElements;
import static kpi.model._helper.Mapper.toOnDemandAggregationElements;
import static kpi.model._helper.MotherObject.kpiDefinition;
import static kpi.model._helper.MotherObject.kpiTable;
import static kpi.model._helper.MotherObject.parameter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.JsonLoaders;
import kpi.model._helper.Serialization;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.complex.ComplexTable;
import kpi.model.complex.element.ComplexAggregationElement;
import kpi.model.complex.table.optional.ComplexTableAggregationPeriod;
import kpi.model.complex.table.optional.ComplexTableDataLookBackLimit;
import kpi.model.complex.table.optional.ComplexTableDataReliabilityOffset;
import kpi.model.complex.table.optional.ComplexTableExportable;
import kpi.model.complex.table.optional.ComplexTableReexportLateData;
import kpi.model.complex.table.required.ComplexTableAggregationElements;
import kpi.model.complex.table.required.ComplexTableAlias;
import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.OnDemandTabularParameter;
import kpi.model.ondemand.ParameterType;
import kpi.model.ondemand.element.OnDemandAggregationElement;
import kpi.model.ondemand.table.optional.OnDemandTableExportable;
import kpi.model.ondemand.table.required.OnDemandTableAggregationElements;
import kpi.model.ondemand.table.required.OnDemandTableAggregationPeriod;
import kpi.model.ondemand.table.required.OnDemandTableAlias;
import kpi.model.simple.SimpleTable;
import kpi.model.simple.element.SimpleAggregationElement;
import kpi.model.simple.table.optional.SimpleTableAggregationPeriod;
import kpi.model.simple.table.optional.SimpleTableDataLookBackLimit;
import kpi.model.simple.table.optional.SimpleTableDataReliabilityOffset;
import kpi.model.simple.table.optional.SimpleTableExportable;
import kpi.model.simple.table.optional.SimpleTableReexportLateData;
import kpi.model.simple.table.required.SimpleTableAggregationElements;
import kpi.model.simple.table.required.SimpleTableAlias;
import kpi.model.simple.table.required.SimpleTableInpDataIdentifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KpiDefinitionTest {

    @Nested
    class Success {

        @Test
        void shouldDeserializeFullHierarchy() {
            final KpiDefinitionRequest actual = Serialization.deserialize(JsonLoaders.load("json/kpi_definition.json"), KpiDefinitionRequest.class);

            final List<OnDemandParameter> parameters = actual.onDemand().parameters();
            Assertions.assertThat(parameters).containsExactlyInAnyOrder(
                    parameter("start_date_time", ParameterType.STRING),
                    parameter("execution_id", ParameterType.LONG)
            );

            final List<OnDemandTabularParameter> tabularParameters = actual.onDemand().tabularParameters();
            Assertions.assertThat(tabularParameters).first().satisfies(tabularParameter -> {
                Assertions.assertThat(tabularParameter.name()).isEqualTo("cell_configuration");
                Assertions.assertThat(tabularParameter.columns()).containsExactlyInAnyOrder(
                        parameter("target_throughtput_r", ParameterType.DOUBLE),
                        parameter("fdn", ParameterType.STRING),
                        parameter("min_rops_for_app_cov_reliability", ParameterType.INTEGER)
                );
            });

            final List<OnDemandTable> onDemandTables = actual.onDemand().kpiOutputTables();
            Assertions.assertThat(onDemandTables).first().satisfies(onDemandTable -> {
                Assertions.assertThat(onDemandTable.aggregationPeriod()).isEqualTo(OnDemandTableAggregationPeriod.of(60));
                Assertions.assertThat(onDemandTable.alias()).isEqualTo(OnDemandTableAlias.of("alias_ondemand"));
                Assertions.assertThat(onDemandTable.aggregationElements()).isEqualTo(OnDemandTableAggregationElements.of(toOnDemandAggregationElements("table.column1", "table.column2")));
                Assertions.assertThat(onDemandTable.exportable()).isEqualTo(OnDemandTableExportable.of(false));

                Assertions.assertThat(onDemandTable.kpiDefinitions()).containsExactlyInAnyOrder(
                        kpiDefinition(
                                "definition_one", "FROM expression_1", "INTEGER[5]", AggregationType.SUM,
                                List.of("table.column1", "table.column2"), true, Collections.emptyList()
                        ),
                        kpiDefinition(
                                "definition_two", "from expression_2", "INTEGER", AggregationType.SUM,
                                List.of("table.column1", "table.column2"), false, Collections.emptyList()
                        ),
                        kpiDefinition(
                                "definition_three", "FROM expression_3", "INTEGER", AggregationType.SUM,
                                List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2")
                        )
                );
            });

            final List<ComplexTable> complexTables = actual.scheduledComplex().kpiOutputTables();
            Assertions.assertThat(complexTables).first().satisfies(complexTable -> {
                Assertions.assertThat(complexTable.aggregationPeriod()).isEqualTo(ComplexTableAggregationPeriod.of(60));
                Assertions.assertThat(complexTable.alias()).isEqualTo(ComplexTableAlias.of("alias_value"));
                Assertions.assertThat(complexTable.aggregationElements()).isEqualTo(ComplexTableAggregationElements.of(toComplexAggregationElements("table.column1", "table.column2")));
                Assertions.assertThat(complexTable.exportable()).isEqualTo(ComplexTableExportable.of(false));
                Assertions.assertThat(complexTable.dataReliabilityOffset()).isEqualTo(ComplexTableDataReliabilityOffset.of(15));
                Assertions.assertThat(complexTable.dataLookBackLimit()).isEqualTo(ComplexTableDataLookBackLimit.of(7_200));
                Assertions.assertThat(complexTable.reexportLateData()).isEqualTo(ComplexTableReexportLateData.of(false));

                Assertions.assertThat(complexTable.kpiDefinitions()).containsExactlyInAnyOrder(
                        kpiDefinition(
                                "definition_one", "FROM expression_1", "INTEGER", AggregationType.SUM, "COMPLEX1",
                                List.of("table.column1", "table.column2"), true, Collections.emptyList(), 15, 7_200, false
                        ),
                        kpiDefinition(
                                "definition_two", "from expression_2", "INTEGER", AggregationType.SUM, "COMPLEX1",
                                List.of("table.column1", "table.column2"), false, Collections.emptyList(), 15, 7_200, false
                        ),
                        kpiDefinition(
                                "definition_three", "FROM expression_3", "INTEGER", AggregationType.SUM, "COMPLEX2",
                                List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2"), 15, 7_200, false
                        )
                );
            });

            final List<SimpleTable> simpleTables = actual.scheduledSimple().kpiOutputTables();
            Assertions.assertThat(simpleTables).first().satisfies(simpleTable -> {
                Assertions.assertThat(simpleTable.aggregationPeriod()).isEqualTo(SimpleTableAggregationPeriod.of(60));
                Assertions.assertThat(simpleTable.alias()).isEqualTo(SimpleTableAlias.of("alias_value"));
                Assertions.assertThat(simpleTable.aggregationElements()).isEqualTo(SimpleTableAggregationElements.of(toAggregationElements("table.column1", "table.column2")));
                Assertions.assertThat(simpleTable.exportable()).isEqualTo(SimpleTableExportable.of(false));
                Assertions.assertThat(simpleTable.dataReliabilityOffset()).isEqualTo(SimpleTableDataReliabilityOffset.of(15));
                Assertions.assertThat(simpleTable.dataLookBackLimit()).isEqualTo(SimpleTableDataLookBackLimit.of(7_200));
                Assertions.assertThat(simpleTable.reexportLateData()).isEqualTo(SimpleTableReexportLateData.of(false));
                Assertions.assertThat(simpleTable.inpDataIdentifier()).isEqualTo(SimpleTableInpDataIdentifier.of("dataSpace|category|parent_schema"));

                Assertions.assertThat(simpleTable.kpiDefinitions()).containsExactlyInAnyOrder(
                        kpiDefinition(
                                "definition_one", "expression_1", "INTEGER", AggregationType.SUM,
                                List.of("table.column1", "table.column2"), true, Collections.emptyList(),
                                15, 7_200, false, "dataSpace|category|child_schema"
                        ),
                        kpiDefinition(
                                "definition_two", "expression_2", "INTEGER", AggregationType.SUM,
                                List.of("table.column1", "table.column2"), false, Collections.emptyList(),
                                15, 7_200, false, "dataSpace|category|parent_schema"
                        ),
                        kpiDefinition(
                                "definition_three", "expression_3", "INTEGER", AggregationType.SUM,
                                List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2"),
                                15, 7_200, false, "dataSpace|category|parent_schema"
                        )
                );
            });
        }

        @Test
        void shouldGiveBackKpiDefinitions() {
            final KpiDefinitionRequest payload = Serialization.deserialize(JsonLoaders.load("json/kpi_definition.json"), KpiDefinitionRequest.class);

            final Set<kpi.model.api.table.definition.KpiDefinition> actual = payload.definitions();

            Assertions.assertThat(actual).containsExactlyInAnyOrder(
                    kpiDefinition(
                            "definition_one", "FROM expression_1", "INTEGER[5]", AggregationType.SUM,
                            List.of("table.column1", "table.column2"), true, Collections.emptyList()
                    ),
                    kpiDefinition(
                            "definition_two", "from expression_2", "INTEGER", AggregationType.SUM,
                            List.of("table.column1", "table.column2"), false, Collections.emptyList()
                    ),
                    kpiDefinition(
                            "definition_three", "FROM expression_3", "INTEGER", AggregationType.SUM,
                            List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2")
                    ),
                    kpiDefinition(
                            "definition_one", "FROM expression_1", "INTEGER", AggregationType.SUM, "COMPLEX1",
                            List.of("table.column1", "table.column2"), true, Collections.emptyList(), 15, 7_200, false
                    ),
                    kpiDefinition(
                            "definition_two", "from expression_2", "INTEGER", AggregationType.SUM, "COMPLEX1",
                            List.of("table.column1", "table.column2"), false, Collections.emptyList(), 15, 7_200, false
                    ),
                    kpiDefinition(
                            "definition_three", "FROM expression_3", "INTEGER", AggregationType.SUM, "COMPLEX2",
                            List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2"), 15, 7_200, false
                    ),
                    kpiDefinition(
                            "definition_one", "expression_1", "INTEGER", AggregationType.SUM,
                            List.of("table.column1", "table.column2"), true, Collections.emptyList(),
                            15, 7_200, false, "dataSpace|category|child_schema"
                    ),
                    kpiDefinition(
                            "definition_two", "expression_2", "INTEGER", AggregationType.SUM,
                            List.of("table.column1", "table.column2"), false, Collections.emptyList(),
                            15, 7_200, false, "dataSpace|category|parent_schema"
                    ),
                    kpiDefinition(
                            "definition_three", "expression_3", "INTEGER", AggregationType.SUM,
                            List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2"),
                            15, 7_200, false, "dataSpace|category|parent_schema"
                    )
            );
        }

        @Test
        void shouldGiveBackListOfTablesOfDefinitions() {
            final KpiDefinitionRequest payload = Serialization.deserialize(JsonLoaders.load("json/kpi_definition.json"), KpiDefinitionRequest.class);

            final List<kpi.model.api.table.Table> actual = payload.tables();

            Assertions.assertThat(actual).containsExactlyInAnyOrder(
                    kpiTable(OnDemandTableAlias.of("alias_ondemand"), 60, null, List.of(OnDemandAggregationElement.of("table.column1"), OnDemandAggregationElement.of("table.column2")),
                            List.of(
                                    kpiDefinition(
                                            "definition_one", "FROM expression_1", "INTEGER[5]", AggregationType.SUM,
                                            List.of("table.column1", "table.column2"), true, Collections.emptyList()
                                    ),
                                    kpiDefinition(
                                            "definition_two", "from expression_2", "INTEGER", AggregationType.SUM,
                                            List.of("table.column1", "table.column2"), false, Collections.emptyList()
                                    ),
                                    kpiDefinition(
                                            "definition_three", "FROM expression_3", "INTEGER", AggregationType.SUM,
                                            List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2")
                                    )
                            )
                    ),
                    kpiTable(ComplexTableAlias.of("alias_value"), 60, null,
                            List.of(ComplexAggregationElement.of("table.column1"), ComplexAggregationElement.of("table.column2")),
                            List.of(
                                    kpiDefinition(
                                            "definition_one", "FROM expression_1", "INTEGER", AggregationType.SUM, "COMPLEX1",
                                            List.of("table.column1", "table.column2"), true, Collections.emptyList(), 15, 7_200, false
                                    ),
                                    kpiDefinition(
                                            "definition_two", "from expression_2", "INTEGER", AggregationType.SUM, "COMPLEX1",
                                            List.of("table.column1", "table.column2"), false, Collections.emptyList(), 15, 7_200, false
                                    ),
                                    kpiDefinition(
                                            "definition_three", "FROM expression_3", "INTEGER", AggregationType.SUM, "COMPLEX2",
                                            List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2"), 15, 7_200, false
                                    )
                            )
                    ),
                    kpiTable(SimpleTableAlias.of("alias_value"), 60, null,
                            List.of(SimpleAggregationElement.of("table.column1"), SimpleAggregationElement.of("table.column2")),
                            SimpleTableInpDataIdentifier.of("dataSpace|category|parent_schema"),
                            List.of(
                                    kpiDefinition(
                                            "definition_one", "expression_1", "INTEGER", AggregationType.SUM,
                                            List.of("table.column1", "table.column2"), true, Collections.emptyList(),
                                            15, 7_200, false, "dataSpace|category|child_schema"
                                    ),
                                    kpiDefinition(
                                            "definition_two", "expression_2", "INTEGER", AggregationType.SUM,
                                            List.of("table.column1", "table.column2"), false, Collections.emptyList(),
                                            15, 7_200, false, "dataSpace|category|parent_schema"
                                    ),
                                    kpiDefinition(
                                            "definition_three", "expression_3", "INTEGER", AggregationType.SUM,
                                            List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2"),
                                            15, 7_200, false, "dataSpace|category|parent_schema"
                                    )
                            )
                    )
            );

        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Failure {

        @MethodSource("provideInvalidRawKpiDefinitionContent")
        @ParameterizedTest(name = "Kpi Definition ''{0}'' raises error: ''{1}''")
        void shouldFailDeserialization(final String content, final String errorMessage) {
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, KpiDefinitionRequest.class))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessageContaining(errorMessage);
        }

        @Test
        void shouldFailOnCollectionLevelRetentionPeriod() {
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(JsonLoaders.load("json/test_kpi_definition_invalid_retention.json"), KpiDefinitionRequest.class))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("Retention period cannot be set on collection level!");
        }

        @Test
        void shouldFailUniqueAliasesValidation() {

            Assertions.assertThatThrownBy(() -> Serialization.deserialize(JsonLoaders.load("json/test_kpi_definitions_invalid_aliases.json"), KpiDefinitionRequest.class))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("'alias' and 'aggregation_period' values must be unique for a KPI type. Received values: 'alias_simple_test_value' and '60'");
        }

        private Stream<Arguments> provideInvalidRawKpiDefinitionContent() {
            final String emptyKpiDefinition = "{ }";
            final String emptyKpiDefinitionError = "At least one kpi definition type is required";
            final String emptyTablesKpiDefinition =
                    "{\n" +
                            "  \"on_demand\":{\n" +
                            "    \"kpi_output_tables\": []\n" +
                            "  }\n" +
                            "}";
            final String emptyTablesKpiDefinitionError = "At least one kpi table is required";
            return Stream.of(Arguments.of(emptyKpiDefinition, emptyKpiDefinitionError),
                    Arguments.of(emptyTablesKpiDefinition, emptyTablesKpiDefinitionError));
        }
    }

    @Nested
    class KpiTypeAliasAggPeriodUniqueness {

        @Test
        void shouldFailOnSameAliasAndAggPeriodWithOnDemandAndScheduledKpiType() {
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(JsonLoaders.load("json/test_kpi_definitions_invalid_kpitypes_in_same_alias.json"), KpiDefinitionRequest.class))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessage("The following tables should only contain ON_DEMAND or SCHEDULED KPI types: '[(alias = alias_simple,aggregation_period = 60)]'");

        }

        @Test
        void shouldNotFailWithOnlyScheduledKpiType() {
            final KpiDefinitionRequest actual = Serialization.deserialize("{" +
                    "  \"scheduled_complex\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"aggregation_period\": 60, \n" +
                    "         \"alias\": \"alias_value\", \n" +
                    "         \"aggregation_elements\": [ \"table.column1\", \"table.column2\" ],\n" +
                    "         \"inp_data_identifier\": \"dataSpace|category|parent_schema\",\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM expression_1\", \n" +
                    "                  \"object_type\": \"INTEGER\", \n" +
                    "                  \"aggregation_type\": \"SUM\", \n" +
                    "                  \"execution_group\": \"COMPLEX1\", \n" +
                    "                  \"exportable\": true,\n" +
                    "                  \"inp_data_identifier\": \"dataSpace|category|child_schema\"\n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  },\n" +
                    "  \"scheduled_simple\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "        \"aggregation_period\": 60,\n" +
                    "        \"alias\": \"alias_value\",\n" +
                    "        \"aggregation_elements\": [\"table.column1\", \"table.column2\"],\n" +
                    "        \"inp_data_identifier\": \"dataSpace|category|parent_schema\",\n" +
                    "        \"kpi_definitions\": [\n" +
                    "          {\n" +
                    "            \"name\": \"definition_one\",\n" +
                    "            \"expression\": \"expression_1\",\n" +
                    "            \"object_type\": \"INTEGER\",\n" +
                    "            \"aggregation_type\": \"SUM\",\n" +
                    "            \"exportable\": true,\n" +
                    "            \"inp_data_identifier\": \"dataSpace|category|child_schema\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "    }]\n" +
                    "  }" +
                    "}", KpiDefinitionRequest.class);
            final List<ComplexTable> complexTables = actual.scheduledComplex().kpiOutputTables();
            Assertions.assertThat(complexTables).first().satisfies(complexTable -> {
                Assertions.assertThat(complexTable.aggregationPeriod()).isEqualTo(ComplexTableAggregationPeriod.of(60));
                Assertions.assertThat(complexTable.alias()).isEqualTo(ComplexTableAlias.of("alias_value"));
                Assertions.assertThat(complexTable.aggregationElements()).isEqualTo(ComplexTableAggregationElements.of(toComplexAggregationElements("table.column1", "table.column2")));
                Assertions.assertThat(complexTable.exportable()).isEqualTo(ComplexTableExportable.of(false));
                Assertions.assertThat(complexTable.dataReliabilityOffset()).isEqualTo(ComplexTableDataReliabilityOffset.of(15));
                Assertions.assertThat(complexTable.dataLookBackLimit()).isEqualTo(ComplexTableDataLookBackLimit.of(7_200));
                Assertions.assertThat(complexTable.reexportLateData()).isEqualTo(ComplexTableReexportLateData.of(false));

                Assertions.assertThat(complexTable.kpiDefinitions()).containsExactlyInAnyOrder(
                        kpiDefinition(
                                "definition_one", "FROM expression_1", "INTEGER", AggregationType.SUM, "COMPLEX1",
                                List.of("table.column1", "table.column2"), true, Collections.emptyList(), 15, 7_200, false
                        )
                );
            });

            final List<SimpleTable> simpleTables = actual.scheduledSimple().kpiOutputTables();
            Assertions.assertThat(simpleTables).first().satisfies(simpleTable -> {
                Assertions.assertThat(simpleTable.aggregationPeriod()).isEqualTo(SimpleTableAggregationPeriod.of(60));
                Assertions.assertThat(simpleTable.alias()).isEqualTo(SimpleTableAlias.of("alias_value"));
                Assertions.assertThat(simpleTable.aggregationElements()).isEqualTo(SimpleTableAggregationElements.of(toAggregationElements("table.column1", "table.column2")));
                Assertions.assertThat(simpleTable.exportable()).isEqualTo(SimpleTableExportable.of(false));
                Assertions.assertThat(simpleTable.dataReliabilityOffset()).isEqualTo(SimpleTableDataReliabilityOffset.of(15));
                Assertions.assertThat(simpleTable.dataLookBackLimit()).isEqualTo(SimpleTableDataLookBackLimit.of(7_200));
                Assertions.assertThat(simpleTable.reexportLateData()).isEqualTo(SimpleTableReexportLateData.of(false));
                Assertions.assertThat(simpleTable.inpDataIdentifier()).isEqualTo(SimpleTableInpDataIdentifier.of("dataSpace|category|parent_schema"));

                Assertions.assertThat(simpleTable.kpiDefinitions()).containsExactlyInAnyOrder(
                        kpiDefinition(
                                "definition_one", "expression_1", "INTEGER", AggregationType.SUM,
                                List.of("table.column1", "table.column2"), true, Collections.emptyList(),
                                15, 7_200, false, "dataSpace|category|child_schema"
                        )
                );
            });

        }

        @Test
        void shouldNotFailWithOnlyOnDemandKpiType() {
            final KpiDefinitionRequest actual = Serialization.deserialize("" +
                    "{\n" +
                    "  \"on_demand\":{\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"aggregation_period\": 60, \n" +
                    "         \"alias\": \"alias_ondemand\",\n" +
                    "         \"aggregation_elements\": [\"table.column1\", \"table.column2\"],\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM expression_1\", \n" +
                    "                  \"object_type\": \"INTEGER[5]\",\n" +
                    "                  \"aggregation_type\": \"SUM\", \n" +
                    "                  \"exportable\": true\n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  }" +
                    "}", KpiDefinitionRequest.class);

            final List<OnDemandTable> onDemandTables = actual.onDemand().kpiOutputTables();
            Assertions.assertThat(onDemandTables).first().satisfies(onDemandTable -> {
                Assertions.assertThat(onDemandTable.aggregationPeriod()).isEqualTo(OnDemandTableAggregationPeriod.of(60));
                Assertions.assertThat(onDemandTable.alias()).isEqualTo(OnDemandTableAlias.of("alias_ondemand"));
                Assertions.assertThat(onDemandTable.aggregationElements()).isEqualTo(OnDemandTableAggregationElements.of(toOnDemandAggregationElements("table.column1", "table.column2")));
                Assertions.assertThat(onDemandTable.exportable()).isEqualTo(OnDemandTableExportable.of(false));

                Assertions.assertThat(onDemandTable.kpiDefinitions()).containsExactlyInAnyOrder(
                        kpiDefinition(
                                "definition_one", "FROM expression_1", "INTEGER[5]", AggregationType.SUM,
                                List.of("table.column1", "table.column2"), true, Collections.emptyList()
                        )
                );
            });

            final List<OnDemandTabularParameter> tabularParameters = actual.onDemand().tabularParameters();
            Assertions.assertThat(tabularParameters).isEmpty();

            final List<OnDemandParameter> parameters = actual.onDemand().parameters();
            Assertions.assertThat(parameters).isEmpty();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class RestrictParametrizationToSpecificFields {

        @MethodSource("provideParameterizedSimpleDefinitionsWithFailed")
        @ParameterizedTest(name = "Kpi parameter in ''{0}'' raises error: ''{2}''")
        void shouldFailSimpleDeserialization(final String testName, final String content, final String errorMessage) {
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, KpiDefinitionRequest.class))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessageContaining(errorMessage);
        }

        @MethodSource("provideParameterizedComplexDefinitionsWithFailed")
        @ParameterizedTest(name = "Kpi parameter in ''{0}'' raises error: ''{2}''")
        void shouldFailComplexDeserialization(final String testName, final String content, final String errorMessage) {
            Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, KpiDefinitionRequest.class))
                    .isInstanceOf(ValueInstantiationException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .getRootCause()
                    .hasMessageContaining(errorMessage);
        }

        @MethodSource("provideAcceptedParameterizedDefinitions")
        @ParameterizedTest(name = "Kpi parameter in ''{0}'' accepted")
        void shouldAcceptDeserialization(final String testName, final String content) {
            Assertions.assertThatCode(() -> Serialization.deserialize(content, KpiDefinitionRequest.class)).doesNotThrowAnyException();
        }

        private Stream<Arguments> provideParameterizedSimpleDefinitionsWithFailed() {
            final String simpleAggregationTable = "{" +
                    "  \"scheduled_simple\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "        \"alias\": \"alias_value\",\n" +
                    "        \"inp_data_identifier\": \"dataSpace|category|parent_schema\",\n" +
                    "        \"aggregation_elements\": [ \"table.column1\", \"'${table.column}' as param\" ],\n" +
                    "        \"kpi_definitions\": [\n" +
                    "          {\n" +
                    "            \"name\": \"definition_one\",\n" +
                    "            \"expression\": \"expression_1\",\n" +
                    "            \"object_type\": \"INTEGER\",\n" +
                    "            \"aggregation_type\": \"SUM\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "    }]\n" +
                    "  }" +
                    "}";
            final String simpleAggregation = "{" +
                    "  \"scheduled_simple\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "        \"alias\": \"alias_value\",\n" +
                    "        \"inp_data_identifier\": \"dataSpace|category|parent_schema\",\n" +
                    "        \"aggregation_elements\": [ \"table.column1\", \"table.column2\" ],\n" +
                    "        \"kpi_definitions\": [\n" +
                    "          {\n" +
                    "            \"name\": \"definition_one\",\n" +
                    "            \"expression\": \"expression_1\",\n" +
                    "            \"object_type\": \"INTEGER\",\n" +
                    "            \"aggregation_elements\": [ \"table.column1\", \"'${table.column}' as param\" ],\n" +
                    "            \"aggregation_type\": \"SUM\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "    }]\n" +
                    "  }" +
                    "}";
            final String simpleExpression = "{" +
                    "  \"scheduled_simple\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "        \"alias\": \"alias_value\",\n" +
                    "        \"inp_data_identifier\": \"dataSpace|category|parent_schema\",\n" +
                    "        \"aggregation_elements\": [ \"table.column1\", \"table.column2\" ],\n" +
                    "        \"kpi_definitions\": [\n" +
                    "          {\n" +
                    "            \"name\": \"definition_one\",\n" +
                    "            \"expression\": \"'${table.column}' as param\",\n" +
                    "            \"object_type\": \"INTEGER\",\n" +
                    "            \"aggregation_type\": \"SUM\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "    }]\n" +
                    "  }" +
                    "}";
            final String simpleFilter = "{" +
                    "  \"scheduled_simple\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "        \"alias\": \"alias_value\",\n" +
                    "        \"inp_data_identifier\": \"dataSpace|category|parent_schema\",\n" +
                    "        \"aggregation_elements\": [ \"table.column1\", \"table.column2\" ],\n" +
                    "        \"kpi_definitions\": [\n" +
                    "          {\n" +
                    "            \"name\": \"definition_one\",\n" +
                    "            \"expression\": \"expression_1\",\n" +
                    "            \"object_type\": \"INTEGER\",\n" +
                    "            \"filters\": [ \"table.column1\", \"'${table.column}' as param\" ],\n" +
                    "            \"aggregation_type\": \"SUM\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "    }]\n" +
                    "  }" +
                    "}";

            final String aggregationElementErrorMessage = "The attribute 'aggregation_element' does not support parameters";
            final String expressionErrorMessage = "The attribute 'expression' does not support parameters";
            final String filerErrorMessage = "The attribute 'filter' does not support parameters";

            return Stream.of(Arguments.of("simpleAggregationTable", simpleAggregationTable, aggregationElementErrorMessage),
                    Arguments.of("simpleAggregation", simpleAggregation, aggregationElementErrorMessage),
                    Arguments.of("simpleExpression", simpleExpression, expressionErrorMessage),
                    Arguments.of("simpleFilter", simpleFilter, filerErrorMessage));
        }

        private Stream<Arguments> provideParameterizedComplexDefinitionsWithFailed() {
            final String complexAggregationTable = "{" +
                    "  \"scheduled_complex\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"alias\": \"alias_value\", \n" +
                    "         \"aggregation_elements\": [ \"table.column1\", \"'${table.column}' as param\" ],\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM expression_1\", \n" +
                    "                  \"object_type\": \"INTEGER\", \n" +
                    "                  \"aggregation_type\": \"SUM\", \n" +
                    "                  \"execution_group\": \"COMPLEX1\" \n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  }\n";
            final String complexAggregation = "{" +
                    "  \"scheduled_complex\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"alias\": \"alias_value\", \n" +
                    "         \"aggregation_elements\": [ \"table.column1\", \"table.param2\" ],\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM expression_1\", \n" +
                    "                  \"object_type\": \"INTEGER\", \n" +
                    "                  \"aggregation_type\": \"SUM\", \n" +
                    "                  \"aggregation_elements\": [ \"table.column1\", \"'${table.column}' as param\" ],\n" +
                    "                  \"execution_group\": \"COMPLEX1\" \n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  }\n";
            final String complexExpression = "{" +
                    "  \"scheduled_complex\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"alias\": \"alias_value\", \n" +
                    "         \"aggregation_elements\": [ \"table.column1\", \"table.param2\" ],\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM '${table.column}' as param\", \n" +
                    "                  \"object_type\": \"INTEGER\", \n" +
                    "                  \"aggregation_type\": \"SUM\", \n" +
                    "                  \"execution_group\": \"COMPLEX1\" \n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  }\n";
            final String complexFilter = "{" +
                    "  \"scheduled_complex\": {\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"alias\": \"alias_value\", \n" +
                    "         \"aggregation_elements\": [ \"table.column1\", \"table.param2\" ],\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM expression_1\", \n" +
                    "                  \"object_type\": \"INTEGER\", \n" +
                    "                  \"aggregation_type\": \"SUM\", \n" +
                    "                  \"filters\": [ \"table.column1\", \"'${table.column}' as param\" ],\n" +
                    "                  \"execution_group\": \"COMPLEX1\" \n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  }\n";

            final String aggregationElementErrorMessage = "The attribute 'aggregation_element' does not support parameters";
            final String expressionErrorMessage = "The attribute 'expression' only support '${param.start_date_time}' or '${param.end_date_time}' as parameter";
            final String filerErrorMessage = "The attribute 'filter' only support '${param.start_date_time}' or '${param.end_date_time}' as parameter";

            return Stream.of(Arguments.of("complexAggregationTable", complexAggregationTable, aggregationElementErrorMessage),
                    Arguments.of("complexAggregation", complexAggregation, aggregationElementErrorMessage),
                    Arguments.of("complexExpression", complexExpression, expressionErrorMessage),
                    Arguments.of("complexFilter", complexFilter, filerErrorMessage)
            );
        }

        private Stream<Arguments> provideAcceptedParameterizedDefinitions() {

            final String onDemandAggregation = "{\n" +
                    "  \"on_demand\":{\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"aggregation_period\": 60, \n" +
                    "         \"alias\": \"alias_ondemand\",\n" +
                    "         \"aggregation_elements\": [\"table.column1\", \"table.column2\"],\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM expression_1\", \n" +
                    "                  \"object_type\": \"INTEGER[5]\",\n" +
                    "                  \"aggregation_elements\": [\"table.column1\", \"'${table.column}' as param\"],\n" +
                    "                  \"aggregation_type\": \"SUM\" \n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  }" +
                    "}";
            final String onDemandExpression = "{\n" +
                    "  \"on_demand\":{\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"aggregation_period\": 60, \n" +
                    "         \"alias\": \"alias_ondemand\",\n" +
                    "         \"aggregation_elements\": [\"table.column1\", \"table.column2\"],\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM '${table.column}' as param\", \n" +
                    "                  \"object_type\": \"INTEGER[5]\",\n" +
                    "                  \"aggregation_type\": \"SUM\" \n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  }" +
                    "}";
            final String onDemandFilter = "{\n" +
                    "  \"on_demand\":{\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"aggregation_period\": 60, \n" +
                    "         \"alias\": \"alias_ondemand\",\n" +
                    "         \"aggregation_elements\": [\"table.column1\", \"table.column2\"],\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM expression_1\", \n" +
                    "                  \"object_type\": \"INTEGER[5]\",\n" +
                    "                  \"filters\": [\"table.column1\", \"'${table.column}' as param\"],\n" +
                    "                  \"aggregation_type\": \"SUM\" \n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  }" +
                    "}";

            final String onDemandAggregationTable = "{\n" +
                    "  \"on_demand\":{\n" +
                    "    \"kpi_output_tables\": [{\n" +
                    "         \"aggregation_period\": 60, \n" +
                    "         \"alias\": \"alias_ondemand\",\n" +
                    "         \"aggregation_elements\": [\"table.column1\", \"'${table.column}' as param\"],\n" +
                    "         \"kpi_definitions\": [ \n" +
                    "              { \n" +
                    "                  \"name\": \"definition_one\", \n" +
                    "                  \"expression\": \"FROM expression_1\", \n" +
                    "                  \"object_type\": \"INTEGER[5]\",\n" +
                    "                  \"aggregation_type\": \"SUM\" \n" +
                    "              }\n" +
                    "         ] \n" +
                    "    }]\n" +
                    "  }" +
                    "}";

            return Stream.of(
                    Arguments.of("onDemandAggregation", onDemandAggregation),
                    Arguments.of("onDemandExpression", onDemandExpression),
                    Arguments.of("onDemandFilter", onDemandFilter),
                    Arguments.of("onDemandAggregationTable", onDemandAggregationTable)
            );
        }
    }

}