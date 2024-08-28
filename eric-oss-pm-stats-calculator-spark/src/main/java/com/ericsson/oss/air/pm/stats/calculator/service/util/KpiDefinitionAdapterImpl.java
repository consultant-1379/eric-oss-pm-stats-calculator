/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Class providing Adapter between:
 * <br>
 * {@link com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition}
 * <br>
 * and
 * <br>
 * {@link com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition}
 *
 */
@Slf4j
@Component
public final class KpiDefinitionAdapterImpl {
    public com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition convertKpiDefinition(
            @NonNull final com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition kpiDefinition
    ) {
        return com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition.builder()
                .withExpression(kpiDefinition.getExpression())
                .withAggregationElements(kpiDefinition.getAggregationElements())
                .withAggregationPeriod(kpiDefinition.getAggregationPeriod().toString())
                .withAlias(kpiDefinition.getAlias())
                .withAggregationType(kpiDefinition.getAggregationType())
                .withDataLookbackLimit(kpiDefinition.getDataLookbackLimit())
                .withDataReliabilityOffset(kpiDefinition.getDataReliabilityOffset())
                .withExecutionGroup(Objects.nonNull(kpiDefinition.getExecutionGroup()) ? kpiDefinition.getExecutionGroup().getExecutionGroup() : null)
                .withFilter(kpiDefinition.getFilters().stream().map(Filter::new).toList())
                .withInpDataIdentifier(getDataIdentifier(kpiDefinition))
                .withExportable(kpiDefinition.getExportable())
                .withName(kpiDefinition.getName())
                .withObjectType(kpiDefinition.getObjectType())
                .withReexportLateData(kpiDefinition.getReexportLateData())
                .withSchemaDetail(getSchemaDetail(kpiDefinition))
                .withCollectionId(kpiDefinition.getCollectionId())
                .build();
    }

    public List<com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition> convertKpiDefinitions(
            @NonNull final List<com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition> kpiDefinitions
    ) {
        return kpiDefinitions.stream().map(this::convertKpiDefinition).toList();
    }

    private DataIdentifier getDataIdentifier(final com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition kpiDefinition) {
        final String dataSpace = kpiDefinition.getSchemaDataSpace();
        final String category = kpiDefinition.getSchemaCategory();
        final String schemaName = kpiDefinition.getSchemaName();

        if (dataSpace == null || category == null || schemaName == null) {
            return null;
        }
        return DataIdentifier.of(dataSpace, category, schemaName);
    }

    private SchemaDetail getSchemaDetail(@NonNull final com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.KpiDefinition kpiDefinition) {
        return Optional.ofNullable(kpiDefinition.getSchemaDetail())
                       .map(KpiDefinitionAdapterImpl::schemaDetail)
                       .orElse(null);
    }

    private static SchemaDetail schemaDetail(@NonNull final com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.SchemaDetail detail) {
        return SchemaDetail.builder().withId(detail.getId()).withTopic(detail.getTopic()).withNamespace(detail.getNamespace()).build();
    }
}
