/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.mapper.SourceMapper;
import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlParserImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.SqlProcessorService;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.extractor.ExpressionExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.expression.visitor.NonSimpleExpressionVisitorImpl;
import com.ericsson.oss.air.pm.stats.common.sqlparser.extractor.LogicalPlanExtractor;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.ExpressionCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.LeafCollector;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.RelationCollectors;

import kpi.model.complex.table.definition.required.ComplexDefinitionExpression;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.execution.SparkSqlParser;

/**
 * Class to parse JSON file containing KPI definitions.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiDefinitionParser {

    private static final SqlProcessorService SQL_PROCESSOR_SERVICE = sqlProcessorService();

    /**
     * Groups KPIs by aliases.
     *
     * @param kpiDefinitions a {@link List} of every KPI to be grouped a {@link Map} of aliases and corresponding KPI definitions.
     * @return a {@link Map} of aliases and corresponding KPIs.
     */
    //TODO move this and dependent private methods to spark module
    public static Map<String, List<KpiDefinition>> groupKpisByAlias(final Collection<KpiDefinition> kpiDefinitions) {
        final Map<String, List<KpiDefinition>> kpiDefinitionsByAlias = new HashMap<>();

        final List<String> aliases = getAllAliasesInDefinition(kpiDefinitions);
        for (final String alias : aliases) {
            kpiDefinitionsByAlias.put(alias, getAllDefinitionsForAlias(kpiDefinitions, alias));
        }

        return Collections.unmodifiableMap(kpiDefinitionsByAlias);
    }

    private static List<KpiDefinition> getAllDefinitionsForAlias(final Collection<KpiDefinition> kpiDefinitions, final String alias) {
        final List<KpiDefinition> requiredKpiDefinition = new ArrayList<>();
        for (final KpiDefinition kpiDefinition : kpiDefinitions) {
            if (kpiDefinition.getAlias().equals(alias)) {
                requiredKpiDefinition.add(kpiDefinition);
            }
        }
        return requiredKpiDefinition;
    }

    private static List<String> getAllAliasesInDefinition(final Collection<KpiDefinition> kpiDefinitions) {
        final Set<String> aliases = new HashSet<>();
        for (final KpiDefinition kpiDefinition : kpiDefinitions) {
            aliases.add(kpiDefinition.getAlias());
        }
        return new ArrayList<>(aliases);
    }

    public static Set<SourceTable> getSourceTables(final KpiDefinition definition) {
        if (definition.isSimple()) {
            return Collections.emptySet();
        }
        return SourceMapper.toSourceTables(collectNonSimpleRelations(definition));
    }

    public static Set<SourceColumn> getSourceColumns(final KpiDefinition definition) {
        if (definition.isSimple()) {
            return Collections.emptySet();
        }
        return SourceMapper.toSourceColumns(collectNonSimpleReferences(definition), collectNonSimpleRelations(definition));
    }

    private static Set<Relation> collectNonSimpleRelations(final KpiDefinition definition) {
        if (definition.isScheduled()) {
            return getRelations(ComplexDefinitionExpression.of(definition.getExpression()));
        } else {
            return getRelations(OnDemandDefinitionExpression.of(definition.getExpression()));
        }
    }

    private static Set<Reference> collectNonSimpleReferences(final KpiDefinition definition) {
        if (definition.isScheduled()) {
            return getReferences(ComplexDefinitionExpression.of(definition.getExpression()));
        } else {
            return getReferences(OnDemandDefinitionExpression.of(definition.getExpression()));
        }
    }

    private static Set<Relation> getRelations(final ExpressionAttribute expression) {
        return RelationCollectors.collect(SQL_PROCESSOR_SERVICE.parsePlan(expression));
    }

    private static Set<Reference> getReferences(final ExpressionAttribute expression) {
        final ExpressionExtractor<Reference> expressionExtractor = new ExpressionExtractor<>(new NonSimpleExpressionVisitorImpl());
        return SQL_PROCESSOR_SERVICE.extractTableColumns(expression, expressionExtractor);
    }

    static SqlProcessorService sqlProcessorService() {
        final SqlParserImpl sqlParser = new SqlParserImpl(new SparkSqlParser(), new LogicalPlanExtractor());
        return new SqlProcessorService(sqlParser, new ExpressionCollector(new LeafCollector()));
    }
}
