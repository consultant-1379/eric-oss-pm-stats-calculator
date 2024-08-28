/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.KpiDefinitionPatchRequest;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(fluent = true)
@Builder(setterPrefix = "with")
public class KpiDefinitionEntity {
    private Integer id;
    private String name;
    private String alias;
    private String expression;
    private String objectType;
    private String aggregationType;
    private Integer aggregationPeriod;
    private List<String> aggregationElements;
    private Boolean exportable;
    private String schemaDataSpace;
    private String schemaCategory;
    private String schemaName;
    private ExecutionGroup executionGroup;
    private Integer dataReliabilityOffset;
    private Integer dataLookbackLimit;
    private List<String> filters;
    private Boolean reexportLateData;
    private SchemaDetail schemaDetail;
    private LocalDateTime timeDeleted;
    private UUID collectionId;

    public boolean isSimple() {
        return schemaName != null;
    }

    public boolean isOnDemand() {
        return executionGroup == null;
    }

    public boolean isComplex() {
        return !isSimple() && !isOnDemand();
    }

    public boolean isDefaultAggregationPeriod() {
        return aggregationPeriod == KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
    }

    public boolean isNotDefaultAggregationPeriod() {
        return !isDefaultAggregationPeriod();
    }

    public String tableName() {
        return isDefaultAggregationPeriod()
                ? String.format("kpi_%s_", alias)
                : String.format("kpi_%s_%s", alias, aggregationPeriod);
    }

    public KpiType kpiType() {
        if (isComplex()) {
            return KpiType.SCHEDULED_COMPLEX;
        }

        return isSimple()
                ? KpiType.SCHEDULED_SIMPLE
                : KpiType.ON_DEMAND;
    }

    public boolean isDeleted() {
        return timeDeleted != null;
    }

    public Table table() {
        return Table.of(tableName());
    }

    @Nullable
    public DataIdentifier dataIdentifier() {
        if (schemaDataSpace == null || schemaCategory == null || schemaName == null) {
            return null;
        }
        return DataIdentifier.of(schemaDataSpace, schemaCategory, schemaName);
    }

    public void update(@NonNull final KpiDefinitionPatchRequest kpiDefinitionPatchRequest) {
        Optional.ofNullable(kpiDefinitionPatchRequest.getExpression()).ifPresent(this::expression);
        Optional.ofNullable(kpiDefinitionPatchRequest.getObjectType()).ifPresent(this::objectType);
        Optional.ofNullable(kpiDefinitionPatchRequest.getFilters()).ifPresent(this::filters);
        Optional.ofNullable(kpiDefinitionPatchRequest.getDataLookbackLimit()).ifPresent(this::dataLookbackLimit);
        Optional.ofNullable(kpiDefinitionPatchRequest.getReexportLateData()).ifPresent(this::reexportLateData);
        Optional.ofNullable(kpiDefinitionPatchRequest.getExportable()).ifPresent(this::exportable);
    }
}
