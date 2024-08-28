/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator;

import static lombok.AccessLevel.PUBLIC;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.service.validator.sql.SqlRelationExtractor;
import com.ericsson.oss.air.pm.stats.service.validator.sql.exception.ExpressionContainsOnlyTabularParametersException;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.RelationReferenceResolution;
import com.ericsson.oss.air.pm.stats.service.validator.sql.reference.ResolutionResult;

import kpi.model.api.table.definition.KpiDefinition;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ExpressionValidator {
    @Inject
    private SqlRelationExtractor sqlRelationExtractor;

    public void validateExpressionNotContainsOnlyTabularParametersDatasource(final List<ResolutionResult> resolutionResults) {

        final List<KpiDefinition> kpiDefinitionsWithOnlyTabularParamRelations = new ArrayList<>();

        for (final ResolutionResult resolutionResult : resolutionResults) {
            final Set<Relation> relations = collectRelations(resolutionResult);

            if (containsOnlyTabularParametersDatasource(relations)) {
                final KpiDefinition kpiDefinition = resolutionResult.kpiDefinition();
                log.warn("'{}' definition has only tabular parameters datasource.", kpiDefinition.name().value());
                kpiDefinitionsWithOnlyTabularParamRelations.add(kpiDefinition);
            }
        }

        if (!kpiDefinitionsWithOnlyTabularParamRelations.isEmpty()) {
            throw new ExpressionContainsOnlyTabularParametersException(kpiDefinitionsWithOnlyTabularParamRelations);
        }
    }

    public void validateExpressionNotContainsOnlyTabularParametersDatasource(final KpiDefinitionEntity kpiDefinitionEntity) {
        final Set<Relation> relations = sqlRelationExtractor.extractRelations(kpiDefinitionEntity);

        if (!relations.isEmpty() && containsOnlyTabularParametersDatasource(relations)) {
            log.warn("'{}' definition would only have tabular parameters datasource.", kpiDefinitionEntity.name());
            throw new ExpressionContainsOnlyTabularParametersException(kpiDefinitionEntity.name(), kpiDefinitionEntity.expression());
        }
    }

    private static Set<Relation> collectRelations(final ResolutionResult resolutionResult) {
        return resolutionResult.resolutions()
                .stream()
                .map(RelationReferenceResolution::relation)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    private static boolean containsOnlyTabularParametersDatasource(final Set<Relation> relations) {
        return relations.stream()
                .allMatch(relation -> relation.hasDatasource(Datasource.TABULAR_PARAMETERS));
    }
}
