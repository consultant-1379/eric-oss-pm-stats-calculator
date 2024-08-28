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

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import kpi.model.api.table.Table;
import kpi.model.api.table.definition.OnDemandKpiDefinitions;
import kpi.model.ondemand.table.optional.OnDemandTableExportable;
import kpi.model.ondemand.table.optional.OnDemandTableRetentionPeriod;
import kpi.model.ondemand.table.required.OnDemandTableAggregationElements;
import kpi.model.ondemand.table.required.OnDemandTableAggregationPeriod;
import kpi.model.ondemand.table.required.OnDemandTableAlias;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@Accessors(fluent = true)
@ToString(includeFieldNames = false)
public class OnDemandTable implements Table {
    @JsonUnwrapped
    private final OnDemandTableAggregationPeriod aggregationPeriod;
    @JsonUnwrapped
    private final OnDemandTableAlias alias;
    @JsonUnwrapped
    private final OnDemandTableAggregationElements aggregationElements;
    @JsonUnwrapped
    private final OnDemandTableExportable exportable;
    @JsonUnwrapped
    private final OnDemandTableRetentionPeriod retentionPeriod;

    @JsonUnwrapped
    private final OnDemandKpiDefinitions kpiDefinitions;

    @Builder
    private OnDemandTable(
            final OnDemandTableAggregationPeriod aggregationPeriod,
            final OnDemandTableAlias alias,
            final OnDemandTableAggregationElements aggregationElements,
            final OnDemandTableExportable exportable,
            final OnDemandTableRetentionPeriod retentionPeriod,
            final OnDemandKpiDefinitions kpiDefinitions
    ) {
        this.aggregationPeriod = aggregationPeriod;
        this.alias = alias;
        this.aggregationElements = aggregationElements;
        this.exportable = exportable;
        this.retentionPeriod = ofNullable(retentionPeriod).orElseGet(OnDemandTableRetentionPeriod::empty);

        this.kpiDefinitions = kpiDefinitions.withInheritedValuesFrom(table());
    }

    private OnDemandTable table() {
        return this;
    }
}
