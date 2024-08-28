/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.ondemand;

import static kpi.model._helper.Mapper.toOnDemandAggregationElements;
import static kpi.model._helper.MotherObject.kpiDefinition;

import java.util.Collections;
import java.util.List;

import kpi.model._helper.Serialization;
import kpi.model.api.enumeration.AggregationType;
import kpi.model.ondemand.table.optional.OnDemandTableExportable;
import kpi.model.ondemand.table.optional.OnDemandTableRetentionPeriod;
import kpi.model.ondemand.table.required.OnDemandTableAggregationElements;
import kpi.model.ondemand.table.required.OnDemandTableAggregationPeriod;
import kpi.model.ondemand.table.required.OnDemandTableAlias;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OnDemandTableTest {

    @Test
    void shouldDeserialize() {
        final String content = '{' +
                "   \"aggregation_period\": 60, " +
                "   \"retention_period_in_days\": 5," +
                "   \"alias\": \"alias_value\", " +
                "   \"aggregation_elements\": [\"table.column1\", \"table.column2\"]," +
                "   \"kpi_definitions\": [ " +
                "        { " +
                "            \"name\": \"definition_one\", " +
                "            \"expression\": \"FROM expression_1\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"exportable\": true" +
                "        }, " +
                "        { " +
                "            \"name\": \"definition_two\", " +
                "            \"expression\": \"FROM expression_2\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"exportable\": false " +
                "        }," +
                "        {" +
                "            \"name\": \"definition_three\"," +
                "            \"expression\": \"FROM expression_3\", " +
                "            \"object_type\": \"INTEGER\", " +
                "            \"aggregation_type\": \"SUM\", " +
                "            \"aggregation_elements\": [\"table.column3\", \"table.column4\"]," +
                "            \"filters\": [\"f_1\", \"f_2\"]" +
                "        }" +
                "   ] " +
                '}';

        final OnDemandTable actual = Serialization.deserialize(content, OnDemandTable.class);

        Assertions.assertThat(actual.aggregationPeriod()).isEqualTo(OnDemandTableAggregationPeriod.of(60));
        Assertions.assertThat(actual.retentionPeriod()).isEqualTo(OnDemandTableRetentionPeriod.of(5));
        Assertions.assertThat(actual.alias()).isEqualTo(OnDemandTableAlias.of("alias_value"));
        Assertions.assertThat(actual.aggregationElements()).isEqualTo(OnDemandTableAggregationElements.of(toOnDemandAggregationElements("table.column1", "table.column2")));
        Assertions.assertThat(actual.exportable()).isEqualTo(OnDemandTableExportable.of(false));

        Assertions.assertThat(actual.kpiDefinitions()).containsExactlyInAnyOrder(
                kpiDefinition(
                        "definition_one", "FROM expression_1", "INTEGER", AggregationType.SUM,
                        List.of("table.column1", "table.column2"), true, Collections.emptyList()
                ),
                kpiDefinition(
                        "definition_two", "FROM expression_2", "INTEGER", AggregationType.SUM,
                        List.of("table.column1", "table.column2"), false, Collections.emptyList()
                ),
                kpiDefinition(
                        "definition_three", "FROM expression_3", "INTEGER", AggregationType.SUM,
                        List.of("table.column3", "table.column4"), false, List.of("f_1", "f_2")
                )
        );
    }
}