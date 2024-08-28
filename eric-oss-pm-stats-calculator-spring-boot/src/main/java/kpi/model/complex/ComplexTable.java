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

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import kpi.model.api.table.Table;
import kpi.model.api.table.definition.ComplexKpiDefinitions;
import kpi.model.complex.table.definition.optional.ComplexTableRetentionPeriod;
import kpi.model.complex.table.optional.ComplexTableAggregationPeriod;
import kpi.model.complex.table.optional.ComplexTableDataLookBackLimit;
import kpi.model.complex.table.optional.ComplexTableDataReliabilityOffset;
import kpi.model.complex.table.optional.ComplexTableExportable;
import kpi.model.complex.table.optional.ComplexTableReexportLateData;
import kpi.model.complex.table.required.ComplexTableAggregationElements;
import kpi.model.complex.table.required.ComplexTableAlias;
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
public class ComplexTable implements Table {
    @JsonUnwrapped
    private final ComplexTableAggregationPeriod aggregationPeriod;
    @JsonUnwrapped
    private final ComplexTableAlias alias;
    @JsonUnwrapped
    private final ComplexTableAggregationElements aggregationElements;
    @JsonUnwrapped
    private final ComplexTableExportable exportable;
    @JsonUnwrapped
    private final ComplexTableDataReliabilityOffset dataReliabilityOffset;
    @JsonUnwrapped
    private final ComplexTableDataLookBackLimit dataLookBackLimit;
    @JsonUnwrapped
    private final ComplexTableReexportLateData reexportLateData;
    @JsonUnwrapped
    private final ComplexTableRetentionPeriod retentionPeriod;
    @JsonUnwrapped
    private final ComplexKpiDefinitions kpiDefinitions;

    @Builder
    @SuppressWarnings("squid:S107")
    private ComplexTable(
            final ComplexTableAggregationPeriod aggregationPeriod,
            final ComplexTableAlias alias,
            final ComplexTableAggregationElements aggregationElements,
            final ComplexTableExportable exportable,
            final ComplexTableDataReliabilityOffset dataReliabilityOffset,
            final ComplexTableDataLookBackLimit dataLookBackLimit,
            final ComplexTableReexportLateData reexportLateData,
            final ComplexTableRetentionPeriod retentionPeriod,
            final ComplexKpiDefinitions kpiDefinitions
    ) {
        this.aggregationPeriod = aggregationPeriod;
        this.alias = alias;
        this.aggregationElements = aggregationElements;
        this.exportable = exportable;
        this.dataReliabilityOffset = dataReliabilityOffset;
        this.dataLookBackLimit = dataLookBackLimit;
        this.reexportLateData = reexportLateData;
        this.retentionPeriod = ofNullable(retentionPeriod).orElseGet(ComplexTableRetentionPeriod::empty);

        this.kpiDefinitions = kpiDefinitions.withInheritedValuesFrom(table());

        ValidationResults.validateAggregationPeriodIsGreaterThanOrEqualToDataReliabilityOffset(this.aggregationPeriod, this.kpiDefinitions);
    }

    private ComplexTable table() {
        return this;
    }
}
