/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.api.table;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.KpiDefinitions;
import kpi.model.api.table.definition.api.AggregationPeriodAttribute;
import kpi.model.api.table.definition.api.AliasAttribute;
import kpi.model.api.table.definition.api.RetentionPeriodAttribute;

public interface Table {
    AliasAttribute alias();

    AggregationPeriodAttribute aggregationPeriod();

    RetentionPeriodAttribute retentionPeriod();

    <T extends KpiDefinition> KpiDefinitions<T> kpiDefinitions();

    default String tableName() {
        final AliasAttribute alias = alias();
        final AggregationPeriodAttribute aggregationPeriod = aggregationPeriod();

        return String.format("kpi_%s_%s", alias.value(), aggregationPeriod.isNotDefault() ? aggregationPeriod.value() : EMPTY);
    }
}
