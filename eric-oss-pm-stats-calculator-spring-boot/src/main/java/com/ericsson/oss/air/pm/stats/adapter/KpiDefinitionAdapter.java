/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.adapter;

import static lombok.AccessLevel.PUBLIC;

import java.util.List;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.cache.SchemaDetailCache;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.common.model.collection.CollectionIdProxy;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.KpiDefinitionRequest;
import kpi.model.api.table.Table;
import kpi.model.api.table.definition.api.AggregationPeriodAttribute;
import kpi.model.api.table.definition.api.AliasAttribute;
import kpi.model.api.table.definition.api.DataLookBackLimitAttribute;
import kpi.model.api.table.definition.api.DataReliabilityOffsetAttribute;
import kpi.model.api.table.definition.api.ExecutionGroupAttribute;
import kpi.model.api.table.definition.api.InpDataIdentifierAttribute;
import kpi.model.api.table.definition.api.ReexportLateDataAttribute;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class KpiDefinitionAdapter {

    private SchemaDetailCache schemaDetailCache;

    public List<KpiDefinitionEntity> toListOfEntities(@NonNull final KpiDefinitionRequest kpiDefinition) {
         return toListOfCollectionEntities(kpiDefinition, CollectionIdProxy.COLLECTION_ID);
    }

    public List<KpiDefinitionEntity> toListOfCollectionEntities(@NonNull final KpiDefinitionRequest kpiDefinition, final UUID collectionId){
        return kpiDefinition.tables().stream()
                            .map(this::transferToEntities)
                            .flatMap(List::stream)
                            .map(entity -> entity.collectionId(collectionId))
                            .toList();
    }

    public List<KpiDefinitionEntity> transferToEntities(final Table table) {
        final AggregationPeriodAttribute aggregationPeriod = table.aggregationPeriod();
        final AliasAttribute alias = table.alias();

        return table.kpiDefinitions().stream()
                    .map(definition -> transferToEntity(definition, aggregationPeriod, alias))
                    .toList();
    }

    private KpiDefinitionEntity transferToEntity(final kpi.model.api.table.definition.KpiDefinition definition,
                                                 final AggregationPeriodAttribute aggregationPeriod,
                                                 final AliasAttribute alias) {

        KpiDefinitionEntity.KpiDefinitionEntityBuilder definitionBuilder = KpiDefinitionEntity.builder();
        definitionBuilder.withName(definition.name().value());
        definitionBuilder.withAlias(alias.value());
        definitionBuilder.withExpression(definition.expression().value());
        definitionBuilder.withObjectType(definition.objectType().originalValue());
        definitionBuilder.withAggregationType(definition.aggregationType().value().name());
        definitionBuilder.withAggregationPeriod(aggregationPeriod.value());
        definitionBuilder.withAggregationElements(definition.aggregationElements().listOfValues());
        definitionBuilder.withExportable(definition.exportable().value());
        definitionBuilder.withFilters(definition.filters().listOfValues());

        tryToSet(() -> {
            ExecutionGroupAttribute attribute = definition.executionGroup();
            definitionBuilder.withExecutionGroup(ExecutionGroup.builder().withName(attribute.value()).build());
        });

        tryToSet(() -> {
            DataReliabilityOffsetAttribute attribute = definition.dataReliabilityOffset();
            definitionBuilder.withDataReliabilityOffset(attribute.value());
        });

        tryToSet(() -> {
            DataLookBackLimitAttribute attribute = definition.dataLookBackLimit();
            definitionBuilder.withDataLookbackLimit(attribute.value());
        });

        tryToSet(() -> {
            ReexportLateDataAttribute attribute = definition.reexportLateData();
            definitionBuilder.withReexportLateData(attribute.value());
        });

        tryToSet(() -> {
            InpDataIdentifierAttribute attribute = definition.inpDataIdentifier();
            definitionBuilder.withSchemaDataSpace(attribute.dataSpace());
            definitionBuilder.withSchemaCategory(attribute.category());
            definitionBuilder.withSchemaName(attribute.schema());
            final SchemaDetail schemaDetail = schemaDetailCache.get(attribute);
            definitionBuilder.withSchemaDetail(schemaDetail);
        });

        return definitionBuilder.build();
    }

    private static void tryToSet(final Runnable setter) {
        try {
            setter.run();
        } catch (final UnsupportedOperationException ignored) { //NOSONAR
            /* Means attribute does not exist for certain KPI Definition type */
        }
    }
}
