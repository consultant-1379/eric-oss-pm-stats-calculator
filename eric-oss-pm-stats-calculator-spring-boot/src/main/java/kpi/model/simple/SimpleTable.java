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

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import kpi.model.api.table.Table;
import kpi.model.api.table.definition.SimpleKpiDefinitions;
import kpi.model.simple.table.optional.SimpleTableAggregationPeriod;
import kpi.model.simple.table.optional.SimpleTableDataLookBackLimit;
import kpi.model.simple.table.optional.SimpleTableDataReliabilityOffset;
import kpi.model.simple.table.optional.SimpleTableExportable;
import kpi.model.simple.table.optional.SimpleTableReexportLateData;
import kpi.model.simple.table.optional.SimpleTableRetentionPeriod;
import kpi.model.simple.table.required.SimpleTableAggregationElements;
import kpi.model.simple.table.required.SimpleTableAlias;
import kpi.model.simple.table.required.SimpleTableInpDataIdentifier;
import kpi.model.util.ValidationResults;
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
public class SimpleTable implements Table {
    @JsonUnwrapped
    private final SimpleTableAggregationPeriod aggregationPeriod;
    @JsonUnwrapped
    private final SimpleTableAlias alias;
    @JsonUnwrapped
    private final SimpleTableAggregationElements aggregationElements;
    @JsonUnwrapped
    private final SimpleTableExportable exportable;
    @JsonUnwrapped
    private final SimpleTableDataReliabilityOffset dataReliabilityOffset;
    @JsonUnwrapped
    private final SimpleTableDataLookBackLimit dataLookBackLimit;
    @JsonUnwrapped
    private final SimpleTableReexportLateData reexportLateData;
    @JsonUnwrapped
    private final SimpleTableInpDataIdentifier inpDataIdentifier;
    @JsonUnwrapped
    private final SimpleTableRetentionPeriod retentionPeriod;

    @JsonUnwrapped
    private final SimpleKpiDefinitions kpiDefinitions;

    @Builder
    @SuppressWarnings("squid:S107")
    private SimpleTable(
            final SimpleTableAggregationPeriod aggregationPeriod,
            final SimpleTableAlias alias,
            final SimpleTableAggregationElements aggregationElements,
            final SimpleTableExportable exportable,
            final SimpleTableDataReliabilityOffset dataReliabilityOffset,
            final SimpleTableDataLookBackLimit dataLookBackLimit,
            final SimpleTableReexportLateData reexportLateData,
            final SimpleTableInpDataIdentifier inpDataIdentifier,
            final SimpleTableRetentionPeriod retentionPeriod,
            final SimpleKpiDefinitions kpiDefinitions
    ) {
        this.aggregationPeriod = aggregationPeriod;
        this.alias = alias;
        this.aggregationElements = aggregationElements;
        this.exportable = exportable;
        this.dataReliabilityOffset = dataReliabilityOffset;
        this.dataLookBackLimit = dataLookBackLimit;
        this.reexportLateData = reexportLateData;
        this.inpDataIdentifier = inpDataIdentifier;
        this.retentionPeriod = ofNullable(retentionPeriod).orElseGet(SimpleTableRetentionPeriod::empty);

        this.kpiDefinitions = kpiDefinitions.withInheritedValuesFrom(table());

        ValidationResults.validateAggregationPeriodIsGreaterThanOrEqualToDataReliabilityOffset(this.aggregationPeriod, this.kpiDefinitions);
    }

    private SimpleTable table() {
        return this;
    }
}
