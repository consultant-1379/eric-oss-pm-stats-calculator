/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.helper;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static lombok.AccessLevel.PUBLIC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiCalculationRequestPayload.TabularParameters;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlExtractorService;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlRelationExtractor;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class TabularParameterHelper {
    @Inject
    private KpiDefinitionService kpiDefinitionService;
    @Inject
    private SqlRelationExtractor sqlRelationExtractor;
    @Inject
    private SqlExtractorService sqlExtractorService;

    public List<String> splitHeader(final TabularParameters tabularParameters) {
        return Arrays.asList(tabularParameters.getHeader().split(","));
    }

    public List<TabularParameters> getTabularParametersWithHeader(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        return kpiCalculationRequestPayload.getTabularParameters().stream()
                .filter(tabularParameter -> tabularParameter.getHeader() != null)
                .collect(Collectors.toList());
    }

    public Set<Reference> getReference(final List<KpiDefinitionEntity> definitions) {
        final Set<Reference> references = new HashSet<>();
        definitions.stream().map(def -> sqlExtractorService.extractColumnsFromOnDemand(def)).forEach(references::addAll);
        return references;
    }

    public MultiValuedMap<KpiDefinitionEntity, Relation> filterRelations(final Set<String> kpiNames, final Datasource datasource) {
        final List<KpiDefinitionEntity> entities = kpiDefinitionService.findByNames(kpiNames);

        final MultiValuedMap<KpiDefinitionEntity, Relation> result = new HashSetValuedHashMap<>();

        for (final KpiDefinitionEntity entity : entities) {
            final Set<Relation> relations = sqlRelationExtractor.extractRelations(entity);

            for (final Relation relation : relations) {
                if (relation.hasDatasource(datasource)) {
                    result.put(entity, relation);
                }
            }
        }

        return result;
    }

    public List<KpiDefinitionEntity> fetchTabularParameterEntities(final KpiCalculationRequestPayload kpiCalculationRequestPayload) {
        final List<KpiDefinitionEntity> definitionsInDatabase = kpiDefinitionService.findByNames(kpiCalculationRequestPayload.getKpiNames());

        final List<KpiDefinitionEntity> tabularParameterDefinitions = new ArrayList<>();
        final Set<String> tabularParameterNames = getTabularParametersWithHeader(kpiCalculationRequestPayload).stream().map(TabularParameters::getName).collect(Collectors.toSet());
        for (final KpiDefinitionEntity definition : definitionsInDatabase) {
            final Set<Relation> relations = sqlRelationExtractor.extractRelations(definition);
            if (hasDatasource(relations, TABULAR_PARAMETERS)) {
                final Set<String> tables = relations.stream().map(Relation::tableName).collect(Collectors.toSet());
                for (final String table : tables) {
                    if (tabularParameterNames.contains(table)) {
                        tabularParameterDefinitions.add(definition);
                    }
                }
            }
        }
        return tabularParameterDefinitions;
    }

    private static boolean hasDatasource(final Collection<Relation> relations, final Datasource datasource) {
        return relations.stream().anyMatch(relation -> relation.hasDatasource(datasource));
    }
}
