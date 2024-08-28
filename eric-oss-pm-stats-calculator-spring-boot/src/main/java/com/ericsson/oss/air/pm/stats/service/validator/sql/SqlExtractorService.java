/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference.Location.AGGREGATION_ELEMENTS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference.Location.EXPRESSION;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference.Location.FILTERS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.markLocation;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.mergeReferences;
import static com.ericsson.oss.air.pm.stats.service.validator.helper.EntityTransformers.toComplex;
import static com.ericsson.oss.air.pm.stats.service.validator.helper.EntityTransformers.toOnDemand;
import static com.ericsson.oss.air.pm.stats.service.validator.helper.EntityTransformers.toSimple;
import static lombok.AccessLevel.PUBLIC;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPathReference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.extractor.ExpressionExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.NonSimpleExpressionVisitorImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.SimpleExpressionVisitorImpl;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.api.table.definition.ComplexKpiDefinitions.ComplexKpiDefinition;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.OnDemandKpiDefinitions.OnDemandKpiDefinition;
import kpi.model.api.table.definition.SimpleKpiDefinitions.SimpleKpiDefinition;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

@ApplicationScoped
@NoArgsConstructor /* EJB definition requires no-arg constructor */
@AllArgsConstructor(access = PUBLIC) /* Internal testing only */
public class SqlExtractorService {
    @Inject
    private SqlProcessorService sqlProcessorService;

    public JsonPathReference extractColumns(final SimpleKpiDefinition simpleKpiDefinition) {
        return doExtractSimpleColumns(simpleKpiDefinition);
    }

    public Set<Reference> extractColumns(final OnDemandKpiDefinition onDemandKpiDefinition) {
        return doExtractNonSimpleColumns(onDemandKpiDefinition);
    }

    public Set<Reference> extractColumns(final ComplexKpiDefinition complexKpiDefinition) {
        return doExtractNonSimpleColumns(complexKpiDefinition);
    }

    public JsonPathReference extractColumnsFromSimple(final KpiDefinitionEntity kpiDefinitionEntity) {
        Validate.isTrue(kpiDefinitionEntity.isSimple(), "The following entity is not simple: %S", kpiDefinitionEntity.name());

        return doExtractSimpleColumns(toSimple(kpiDefinitionEntity));
    }

    public Set<Reference> extractColumnsFromOnDemand(final KpiDefinitionEntity kpiDefinitionEntity) {
        Validate.isTrue(kpiDefinitionEntity.isOnDemand(), "The following entity is not on-demand: '%s'", kpiDefinitionEntity.name());

        return doExtractNonSimpleColumns(toOnDemand(kpiDefinitionEntity));
    }

    public Set<Reference> extractColumnsFromComplex(final KpiDefinitionEntity kpiDefinitionEntity) {
        Validate.isTrue(kpiDefinitionEntity.isComplex(), "The following entity is not complex: '%s'", kpiDefinitionEntity.name());

        return doExtractNonSimpleColumns(toComplex(kpiDefinitionEntity));
    }

    private Set<Reference> doExtractNonSimpleColumns(@NonNull final KpiDefinition kpiDefinition) {
        final ExpressionExtractor<Reference> expressionExtractor = new ExpressionExtractor<>(new NonSimpleExpressionVisitorImpl());
        final Set<Reference> expression = sqlProcessorService.extractTableColumns(kpiDefinition.expression(), expressionExtractor);
        final Set<Reference> filters = sqlProcessorService.extractFilterTableColumns(kpiDefinition.filters());
        final Set<Reference> aggregationElements = sqlProcessorService.extractAggregationElementTableColumns(kpiDefinition.aggregationElements());

        markLocation(expression, EXPRESSION);
        markLocation(filters, FILTERS);
        markLocation(aggregationElements, AGGREGATION_ELEMENTS);

        return mergeReferences(expression, filters, aggregationElements);
    }

    private JsonPathReference doExtractSimpleColumns(final SimpleKpiDefinition simpleKpiDefinition) {
        final JsonPathReference jsonPathReference = JsonPathReference.of();

        final ExpressionExtractor<JsonPath> expressionExtractor = new ExpressionExtractor<>(new SimpleExpressionVisitorImpl());
        jsonPathReference.addAllJsonPaths(sqlProcessorService.extractTableColumns(simpleKpiDefinition.expression(), expressionExtractor));
        jsonPathReference.addAllReferences(sqlProcessorService.extractFilterTableColumns(simpleKpiDefinition.filters()));
        jsonPathReference.addAllReferences(sqlProcessorService.extractAggregationElementTableColumns(simpleKpiDefinition.aggregationElements()));

        return jsonPathReference;
    }
}
