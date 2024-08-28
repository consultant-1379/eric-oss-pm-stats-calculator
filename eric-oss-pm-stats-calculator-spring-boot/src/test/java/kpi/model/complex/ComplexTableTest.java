/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.complex;

import static kpi.model._helper.Mapper.toComplexAggregationElements;
import static kpi.model._helper.MotherObject.kpiDefinition;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import kpi.model._helper.Serialization;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.complex.table.definition.optional.ComplexTableRetentionPeriod;
import kpi.model.complex.table.optional.ComplexTableAggregationPeriod;
import kpi.model.complex.table.optional.ComplexTableDataLookBackLimit;
import kpi.model.complex.table.optional.ComplexTableDataReliabilityOffset;
import kpi.model.complex.table.optional.ComplexTableExportable;
import kpi.model.complex.table.optional.ComplexTableReexportLateData;
import kpi.model.complex.table.required.ComplexTableAggregationElements;
import kpi.model.complex.table.required.ComplexTableAlias;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ComplexTableTest {

    @Test
    void shouldDeserialize() {
        final String content = '{' +
                "   \"aggregation_period\": 60," +
                "   \"retention_period_in_days\": 5," +
                "   \"alias\": \"alias_value\", " +
                "   \"aggregation_elements\": [\"table.column1\", \"table.column2\"]," +
                "   \"kpi_definitions\": [ " +
                "        { " +
                "            \"name\": \"definition_one\", " +
                "            \"expression\": \"FROM expression_1\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"execution_group\": \"COMPLEX1\", " +
                "            \"exportable\": true" +
                "        }, " +
                "        { " +
                "            \"name\": \"definition_two\", " +
                "            \"expression\": \"FROM expression_2\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"execution_group\": \"COMPLEX1\", " +
                "            \"exportable\": false, " +
                "            \"reexport_late_data\": true " +
                "        }," +
                "        {" +
                "            \"name\": \"definition_three\"," +
                "            \"expression\": \"FROM expression_3\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"execution_group\": \"COMPLEX2\", " +
                "            \"aggregation_elements\": [\"table.column3\", \"table.column4\"]," +
                "            \"filters\": [\"f_1\", \"f_2\"]" +
                "        }" +
                "   ] " +
                '}';

        final ComplexTable actual = Serialization.deserialize(content, ComplexTable.class);

        Assertions.assertThat(actual.aggregationPeriod()).isEqualTo(ComplexTableAggregationPeriod.of(60));
        Assertions.assertThat(actual.retentionPeriod()).isEqualTo(ComplexTableRetentionPeriod.of(5));
        Assertions.assertThat(actual.alias()).isEqualTo(ComplexTableAlias.of("alias_value"));
        Assertions.assertThat(actual.aggregationElements()).isEqualTo(ComplexTableAggregationElements.of(toComplexAggregationElements("table.column1", "table.column2")));
        Assertions.assertThat(actual.exportable()).isEqualTo(ComplexTableExportable.of(false));
        Assertions.assertThat(actual.dataReliabilityOffset()).isEqualTo(ComplexTableDataReliabilityOffset.of(15));
        Assertions.assertThat(actual.dataLookBackLimit()).isEqualTo(ComplexTableDataLookBackLimit.of(7_200));
        Assertions.assertThat(actual.reexportLateData()).isEqualTo(ComplexTableReexportLateData.of(false));

        Assertions.assertThat(actual.kpiDefinitions()).containsExactlyInAnyOrder(
                kpiDefinition(
                        "definition_one", "FROM expression_1", "INTEGER", AggregationType.SUM, "COMPLEX1",
                        List.of("table.column1", "table.column2"), true, Collections.emptyList(), 15, 7_200, false
                ),
                kpiDefinition(
                        "definition_two", "FROM expression_2", "INTEGER", AggregationType.SUM, "COMPLEX1",
                        List.of("table.column1", "table.column2"), false, Collections.emptyList(), 15, 7_200, true
                ),
                kpiDefinition(
                        "definition_three", "FROM expression_3", "INTEGER", AggregationType.SUM, "COMPLEX2",
                        List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2"), 15, 7_200, false
                )
        );
    }

    @Test
    void shouldFailValidator() {
        final String content = '{' +
                "   \"aggregation_period\": 60, " +
                "   \"alias\": \"alias_value\", " +
                "   \"data_reliability_offset\": 10, " +
                "   \"aggregation_elements\": [\"table.column1\", \"table.column2\"]," +
                "   \"kpi_definitions\": [ " +
                "        { " +
                "            \"name\": \"definition_one\", " +
                "            \"expression\": \"FROM expression_1\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"execution_group\": \"COMPLEX1\", " +
                "            \"exportable\": true," +
                "            \"data_reliability_offset\": 80" +
                "        }" +
                "   ] " +
                '}';

        Assertions.assertThatThrownBy(() -> Serialization.deserialize(content, ComplexTable.class))
                .isInstanceOf(ValueInstantiationException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .getRootCause()
                .hasMessage("'data_reliability_offset' value '80' must be smaller than 'aggregation_period' value '60'");
    }
}
