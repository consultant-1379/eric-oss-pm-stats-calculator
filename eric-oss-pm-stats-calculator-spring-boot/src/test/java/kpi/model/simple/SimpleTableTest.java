/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.simple;

import static kpi.model._helper.Mapper.toAggregationElements;
import static kpi.model._helper.MotherObject.kpiDefinition;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.simple.table.optional.SimpleTableAggregationPeriod;
import kpi.model.simple.table.optional.SimpleTableDataLookBackLimit;
import kpi.model.simple.table.optional.SimpleTableDataReliabilityOffset;
import kpi.model.simple.table.optional.SimpleTableExportable;
import kpi.model.simple.table.optional.SimpleTableReexportLateData;
import kpi.model.simple.table.optional.SimpleTableRetentionPeriod;
import kpi.model.simple.table.required.SimpleTableAggregationElements;
import kpi.model.simple.table.required.SimpleTableAlias;
import kpi.model.simple.table.required.SimpleTableInpDataIdentifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleTableTest {

    @Test
    void shouldDeserialize() {
        final String content = '{' +
                "   \"aggregation_period\": 60, " +
                "   \"alias\": \"alias_value\", " +
                "   \"aggregation_elements\": [\"table.column1\", \"table.column2\"]," +
                "   \"inp_data_identifier\": \"dataSpace|category|parent_schema\", " +
                "   \"retention_period_in_days\": 5," +
                "   \"kpi_definitions\": [ " +
                "        { " +
                "            \"name\": \"definition_one\", " +
                "            \"expression\": \"expression_1\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"exportable\": true," +
                "            \"inp_data_identifier\": \"dataSpace|category|child_schema\" " +
                "        }, " +
                "        { " +
                "            \"name\": \"definition_two\", " +
                "            \"expression\": \"expression_2\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"exportable\": false, " +
                "            \"reexport_late_data\": true " +
                "        }," +
                "        {" +
                "            \"name\": \"definition_three\"," +
                "            \"expression\": \"expression_3\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"aggregation_elements\": [\"table.column3\", \"table.column4\"]," +
                "            \"filters\": [\"f_1\", \"f_2\"]" +
                "        }" +
                "   ] " +
                '}';

        final SimpleTable actual = Serialization.deserialize(content, SimpleTable.class);

        Assertions.assertThat(actual.aggregationPeriod()).isEqualTo(SimpleTableAggregationPeriod.of(60));
        Assertions.assertThat(actual.alias()).isEqualTo(SimpleTableAlias.of("alias_value"));
        Assertions.assertThat(actual.aggregationElements()).isEqualTo(SimpleTableAggregationElements.of(toAggregationElements("table.column1", "table.column2")));
        Assertions.assertThat(actual.exportable()).isEqualTo(SimpleTableExportable.of(false));
        Assertions.assertThat(actual.dataReliabilityOffset()).isEqualTo(SimpleTableDataReliabilityOffset.of(15));
        Assertions.assertThat(actual.dataLookBackLimit()).isEqualTo(SimpleTableDataLookBackLimit.of(7_200));
        Assertions.assertThat(actual.reexportLateData()).isEqualTo(SimpleTableReexportLateData.of(false));
        Assertions.assertThat(actual.inpDataIdentifier()).isEqualTo(SimpleTableInpDataIdentifier.of("dataSpace|category|parent_schema"));
        Assertions.assertThat(actual.retentionPeriod()).isEqualTo(SimpleTableRetentionPeriod.of(5));

        Assertions.assertThat(actual.kpiDefinitions()).containsExactlyInAnyOrder(
                kpiDefinition(
                        "definition_one", "expression_1", "INTEGER", AggregationType.SUM,
                        List.of("table.column1", "table.column2"), true, Collections.emptyList(),
                        15, 7_200, false, "dataSpace|category|child_schema"
                ),
                kpiDefinition(
                        "definition_two", "expression_2", "INTEGER", AggregationType.SUM,
                        List.of("table.column1", "table.column2"), false, Collections.emptyList(),
                        15, 7_200, true, "dataSpace|category|parent_schema"
                ),
                kpiDefinition(
                        "definition_three", "expression_3", "INTEGER", AggregationType.SUM,
                        List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2"),
                        15, 7_200, false, "dataSpace|category|parent_schema"
                )
        );
    }

    @Test
    void shouldFailValidator() {
        final String content = '{' +
                "   \"aggregation_period\": 60, " +
                "   \"alias\": \"alias_value\", " +
                "   \"aggregation_elements\": [\"table.column1\", \"table.column2\"]," +
                "   \"inp_data_identifier\": \"dataSpace|category|parent_schema\", " +
                "   \"data_reliability_offset\": 10, " +
                "   \"kpi_definitions\": [ " +
                "        { " +
                "            \"name\": \"definition_one\", " +
                "            \"expression\": \"expression_1\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"is_exported\": true," +
                "            \"data_reliability_offset\": 80, " +
                "            \"inp_data_identifier\": \"dataSpace|category|child_schema\" " +
                "        }" +
                "   ] " +
                '}';

        Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, SimpleTable.class))
                .isInstanceOf(ValueInstantiationException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .getRootCause()
                .hasMessage("'data_reliability_offset' value '80' must be smaller than 'aggregation_period' value '60'");
    }
}