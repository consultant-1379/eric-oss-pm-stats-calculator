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

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiAggregationType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiNameUtils;
import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkCollections;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkTrees;
import io.vavr.Predicates;
import kpi.model.complex.table.definition.required.ComplexDefinitionExpression;
import kpi.model.ondemand.table.definition.required.OnDemandDefinitionExpression;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.spark.sql.catalyst.FunctionIdentifier;
import org.apache.spark.sql.catalyst.analysis.SimpleFunctionRegistry;
import org.apache.spark.sql.catalyst.analysis.SimpleFunctionRegistryProvider;
import org.apache.spark.sql.catalyst.analysis.UnresolvedFunction;
import org.apache.spark.sql.catalyst.expressions.Cast;
import org.apache.spark.sql.catalyst.expressions.ExpressionInfo;
import org.apache.spark.sql.catalyst.expressions.aggregate.First;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.catalyst.trees.TreeNode;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.MATHEMATICAL_OPERATORS;
import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_ON_WHITESPACE;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiAggregationType.FIRST;
import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.DATASOURCE_DELIMITER;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@Slf4j
@Component
@AllArgsConstructor
public class SqlExpressionHelperImpl {
    private static final SimpleFunctionRegistry SIMPLE_FUNCTION_REGISTRY = SimpleFunctionRegistryProvider.simpleFunctionRegistry();

    /**
     * Built-in aggregate functions from {@link ExpressionInfo#validGroups}<br>
     * <a href="https://spark.apache.org/docs/latest/sql-ref-functions-builtin.html#aggregate-functions">Built-in aggregate functions</a>
     */
    private static final String GROUP_AGGREGATION_FUNCTION = "agg_funcs";

    private static final String TYPE_AND_EXPRESSION_FORMAT = "%s(%s)";
    private static final String FIRST_TYPE_AND_EXPRESSION_FORMAT = "%s(%s, true)";
    private static final String LEADING_BRACKET = "(";
    private static final String TRAILING_BRACKET = ")";
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";
    private static final String SPACE = " ";
    private static final Pattern FROM_PATTERN = Pattern.compile(" FROM ", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHERE_PATTERN = Pattern.compile(" WHERE ", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_TO_SPLIT_ON_DB_DELIMITER = Pattern.compile("://");

    private SqlProcessorDelegator sqlProcessorDelegator;

    /**
     * Get select-expression from expression based on splitting on FROM.
     *
     * @param expression expression
     * @return from-expression
     */
    public String getSelectExpression(final String expression) {
        return FROM_PATTERN.split(expression, 2)[0];
    }

    /**
     * Get where-expression from from-expression based on splitting on WHERE.
     *
     * @param fromExpression from-expression
     * @return from-expression
     */
    public Optional<String> getWhereExpression(final String fromExpression) {
        if (WHERE_PATTERN.split(fromExpression, 2).length == 1) {
            return Optional.empty();
        }

        return Optional.of(WHERE_PATTERN.split(fromExpression, 2)[1]);
    }

    /**
     * Get source-expression from from-expression based on splitting on WHERE.
     *
     * @param fromExpression from-expression
     * @return from-expression
     */
    public String getSourceExpression(final String fromExpression) {
        return WHERE_PATTERN.split(fromExpression, 2)[0];
    }

    /**
     * Get datasource from expression token, based on splitting on string '://'.
     * Assumes that string :// exists in token.
     *
     * @param sourceExpressionToken expression-token
     * @return datasource
     */
    public String getSourceFromExpressionToken(final String sourceExpressionToken) {
        return PATTERN_TO_SPLIT_ON_DB_DELIMITER.split(sourceExpressionToken, 2)[0];
    }

    /**
     * Get table from expression token, based on splitting on string '://'.
     * Assumes that string :// exists in token.
     *
     * @param sourceExpressionToken expression-token
     * @return table
     */
    public String getTableFromExpressionToken(final String sourceExpressionToken) {
        return PATTERN_TO_SPLIT_ON_DB_DELIMITER.split(sourceExpressionToken, 2)[1];
    }

    /**
     * Creates an SQL filter expression from the provided {@link List} of {@link Filter}.
     *
     * @param filters
     *         {@link List} of {@link Filter} to process.
     * @return an <strong>AND</strong> joined SQL filter expression.
     */
    public String filter(@NonNull final List<? extends Filter> filters) {
        return filters.stream()
                      .filter(Objects::nonNull)
                      .filter(Predicates.not(Filter::isEmpty))
                      .map(Filter::getName)
                      .map(filter -> String.format("(%s)", filter))
                      .collect(Collectors.joining(" AND "));
    }

    /**
     * Parses the select expressions of KPI Definitions to a comma separated list.
     *
     * @param kpis the {@link List} of {@link KpiDefinition}s to create the SQL expression for
     * @return a {@link String} containing a comma separated list of {@link KpiDefinition} SQL expressions
     */
    public String collectSelectExpressionsAsSql(final List<KpiDefinition> kpis) {
        final List<String> result = new ArrayList<>(kpis.size());
        for (final KpiDefinition kpiDefinition : kpis) {
            if (kpiDefinition.isSimple()) {
                String expression = parseSelectExpressionToSql(kpiDefinition);
                for (final JsonPath jsonPath : sqlProcessorDelegator.jsonPaths(expression)) {
                    final String originalColumn = jsonPath.parts().stream().map(JsonPath.Part::name).collect(joining(DOT));

                    List<String> jsonPathList = jsonPath.parts().stream().map(JsonPath.Part::name).collect(Collectors.toList());
                    final String newColumn = jsonPathList.get(0) + DOT + String.join(UNDERSCORE, jsonPathList.subList(1, jsonPathList.size()));

                    expression = expression.replace(originalColumn, newColumn);
                }
                result.add(expression);
            } else {
                result.add(parseSelectExpressionToSql(kpiDefinition));
            }
        }
        return String.join(KpiCalculatorConstants.COMMA_AND_SPACE_SEPARATOR, result);
    }

    /**
     * Gets and parses select expression from definition.
     *
     * @param kpi kpi definition
     * @return select expression in SQL format
     */
    private String parseSelectExpressionToSql(final KpiDefinition kpi) {
        String selectExpression = getSelectExpression(kpi.getExpression());

        final Map<String, String> values = new HashMap<>(2);

        if (isAggregationTypePresentInExpression(selectExpression)) {
            values.put("expression", selectExpression);
        } else {
            if (doesNeedToBeWrappedByAggregationType(selectExpression)) {
                values.put("expression", String.format(TYPE_AND_EXPRESSION_FORMAT, kpi.getAggregationType(), selectExpression));
            } else {
                values.put("expression", combineSelectExprAndAggregationType(selectExpression, kpi.getAggregationType()));
            }
        }

        values.put("kpiName", kpi.getName());

        final String sqlTemplate = "${expression} AS ${kpiName}";
        return StringSubstitutor.replace(sqlTemplate, values);
    }

    private boolean doesNeedToBeWrappedByAggregationType(final String selectExpression) {
        final Set<String> functionNames = collectSparkFunctionNames(selectExpression);

        if (functionNames.isEmpty()) {
            //  Fallback option to other checks - we want to wrap only if it contains non-aggregate function
            return false;
        }

        for (final String functionName : functionNames) {
            if (isAggregationFunction(functionName)) {
                log.info("Expression already contains an aggregate function '{}': '{}'", functionName, selectExpression);
                return false;
            }
        }

        return true;
    }

    /**
     * The function name in {@link UnresolvedFunction} is always the last part for example:
     * <pre>{@code
     *  kpi_sector_60.TO_DATE(aggregation_begin_time) -> TO_DATE
     * }</pre>
     * <p>
     * Spark treats <strong>FIRST</strong> as a separate object {@link First} and not {@link UnresolvedFunction}. We can add <strong>FIRST</strong>
     * safely to the returned {@link Set} as {@link SqlExpressionHelperImpl#SIMPLE_FUNCTION_REGISTRY} does know it - {@link First} is part of the
     * built-in aggregate functions (<strong>agg_funcs</strong>) - even though it is not an {@link UnresolvedFunction}.
     *
     * @param selectExpression cleaned select expression - without <strong>SELECT</strong> and <strong>FROM</strong>
     * @return {@link Set} of resolved function names
     */
    private Set<String> collectSparkFunctionNames(final String selectExpression) {
        final LogicalPlan logicalPlan = toLogicalPlan(selectExpression);
        final Set<String> functionNames = new HashSet<>();

        treeNodes(logicalPlan).forEach(treeNode -> {
            if (treeNode instanceof UnresolvedFunction) {
                final UnresolvedFunction unresolvedFunction = (UnresolvedFunction) treeNode;
                final List<String> functionParts = SparkCollections.asJavaList(unresolvedFunction.nameParts());

                //  The function name is always the last part: kpi_db://kpi_sector_60.TO_DATE(aggregation_begin_time) -> TO_DATE
                final String functionName = functionParts.get(functionParts.size() - 1);
                functionNames.add(functionName);
            } else if (treeNode instanceof First) {
                final First first = (First) treeNode;
                functionNames.add(first.prettyName());
            } else if (treeNode instanceof Cast) {
                final Cast cast = (Cast) treeNode;
                functionNames.add(cast.prettyName());
            }
        });

        return functionNames;
    }

    private static boolean isAggregationFunction(final String funcName) {
        return lookUpFunction(funcName).map(expressionInfo -> {
            final String group = expressionInfo.getGroup();
            return GROUP_AGGREGATION_FUNCTION.equals(group);
        }).orElseThrow(() -> new IllegalStateException(String.format("Function is not known '%s'", funcName)));
    }

    private static Optional<ExpressionInfo> lookUpFunction(final String funcName) {
        return ofNullable(SIMPLE_FUNCTION_REGISTRY.lookupFunction(new FunctionIdentifier(funcName)).getOrElse(null));
    }

    private LogicalPlan toLogicalPlan(final String selectExpression) {
        return sqlProcessorDelegator.parsePlan(new ExpressionAttribute() {
            @Override public String value() { return "SELECT " + selectExpression + " FROM temp"; }
            @Override public String name() { return "SqlExpressionHelperImpl.toLogicalPlan"; }
            @Override public boolean isRequired() { return false; }
        });
    }

    private String combineSelectExprAndAggregationType(final String selectExpression, final String aggregationType) {
        return Arrays.stream(PATTERN_TO_SPLIT_ON_WHITESPACE.split(selectExpression))
                .map(expressionToken -> wrapExpressionTokenWithAggregationType(aggregationType, expressionToken))
                .collect(joining(" "));
    }

    private String wrapExpressionTokenWithAggregationType(final String aggregationType, final String expressionToken) {
        final StringBuilder result = new StringBuilder();

        if (expressionToken.startsWith(LEADING_BRACKET)) {
            result.append(LEADING_BRACKET);
        }

        final String expressionTokenWithoutBrackets = removeLeadingAndTrailingBrackets(expressionToken);
        if (isNumberOrMathematicalOperator(expressionTokenWithoutBrackets) || NumberUtils.isCreatable(expressionTokenWithoutBrackets)) {
            result.append(expressionTokenWithoutBrackets);
        } else {
            final String aggregationTypeFormat = aggregationType.equals(FIRST.getJsonType())
                    ? FIRST_TYPE_AND_EXPRESSION_FORMAT
                    : TYPE_AND_EXPRESSION_FORMAT;
            result.append(String.format(aggregationTypeFormat, aggregationType, expressionTokenWithoutBrackets));
        }

        if (expressionToken.endsWith(TRAILING_BRACKET)) {
            result.append(TRAILING_BRACKET);
        }
        return result.toString();
    }

    /**
     * Checks whether the expression contains any defined {@link KpiAggregationType} and a leading bracket.
     * Case is ignored.
     *
     * @param selectExpression expression to be checked
     * @return result of the check
     */
    private boolean isAggregationTypePresentInExpression(final String selectExpression) {
        final List<KpiAggregationType> aggregationTypes = KpiAggregationType.valuesAsList();
        final Set<String> sparkFunctionNames = collectSparkFunctionNames(selectExpression);

        for (final String functionName : sparkFunctionNames) {
            for (final KpiAggregationType aggregationType : aggregationTypes) {
                if (functionName.equalsIgnoreCase(aggregationType.getJsonType())) {
                    return true;
                }
            }
        }

        return false;
    }

    private String removeLeadingAndTrailingBrackets(final String input) {
        return input.substring(
                input.startsWith(LEADING_BRACKET) ? 1 : 0,
                input.endsWith(TRAILING_BRACKET) ? (input.length() - 1) : input.length());
    }

    private boolean isNumberOrMathematicalOperator(final String cleanExpressionToken) {
        return MATHEMATICAL_OPERATORS.matcher(cleanExpressionToken).find() || StringUtils.isNumeric(cleanExpressionToken);
    }

    /**
     * Get from-expression from expression based on splitting on {@link SqlExpressionHelperImpl#FROM_PATTERN}.
     * In case of missing <strong>FROM</strong> (e.g. Simple KPI), returns the schema.
     *
     * @param definition KPI definition
     * @return from-expression or schema name
     */
    public String getFromExpression(final KpiDefinition definition) {
        if (definition.isSimple()) {
            final DataIdentifier inpDataIdentifier = definition.getInpDataIdentifier();
            return inpDataIdentifier.schema();
        }

        return FROM_PATTERN.split(definition.getExpression(), 2)[1];
    }

    /**
     * In case of simple definitions, returns the schema from the input data identifier.
     * <br>
     * In case of complex and on-demand definitions, returns the from-expression without data sources.
     * In case of {@link Datasource#KPI_IN_MEMORY} and {@link Datasource#KPI_POST_AGGREGATION} data sources, additionally
     * temporary dataset view is added.
     * Where-expression is not modified.
     * <br><b>NOTE:</b> The method assumes there is only one 'FROM' in the KPI expression
     *
     * @param definition KPI definition
     * @return the cleaned from-expression or the schema
     */
    public String getCleanedFromExpressionOrSchema(final KpiDefinition definition) {
        if (definition.isSimple() && Objects.nonNull(definition.getInpDataIdentifier())) {
            return definition.getInpDataIdentifier().schema();
        }
        return getFromExpressionWithoutSource(getFromExpression(definition));
    }

    /**
     * Returns from-expression without datasource or source table. Where-expression is not modified.
     * In case of {@link Datasource#KPI_IN_MEMORY} and {@link Datasource#KPI_POST_AGGREGATION} data sources, additionally
     * temporary dataset view is added.
     * <br><b>NOTE:</b> Valid for complex, on-demand from-expressions.
     *
     * @param fromExpression from-expression
     * @return the cleaned SQL source-expression
     */
    @java.lang.SuppressWarnings("squid:S3655")
    public String getFromExpressionWithoutSource(final String fromExpression) {
        final String sourceExpression = getSourceExpression(fromExpression);
        final String cleanedSourceExpression = Arrays.stream(PATTERN_TO_SPLIT_ON_WHITESPACE.split(sourceExpression))
                .map(this::cleanSourceFromExpressionToken)
                .collect(Collectors.joining(SPACE));

        if (getWhereExpression(fromExpression).isPresent()) {
            return String.format("%1$s WHERE %2$s", cleanedSourceExpression, getWhereExpression(fromExpression).get());
        }
        return cleanedSourceExpression;
    }

    /**
     * Cleans data sources from expression token
     * In case of {@link Datasource#KPI_IN_MEMORY} and {@link Datasource#KPI_POST_AGGREGATION} data sources
     * temporary dataset view is added.
     *
     * @param sourceExpressionToken expression token
     * @return cleaned expression token
     */
    private String cleanSourceFromExpressionToken(final String sourceExpressionToken) {
        final boolean hasLeadingBracket = sourceExpressionToken.startsWith(LEADING_BRACKET);
        String sourceToken = hasLeadingBracket ? sourceExpressionToken.substring(1) : sourceExpressionToken;
        if (sourceToken.contains(DATASOURCE_DELIMITER)) {
            String dataSource = getSourceFromExpressionToken(sourceExpressionToken);
            String table = getTableFromExpressionToken(sourceExpressionToken);

            if (Datasource.KPI_IN_MEMORY.getName().equals(dataSource)
                    || Datasource.KPI_POST_AGGREGATION.getName().equals(dataSource)) {
                String tempDataViewName = KpiNameUtils.createTemporaryDatasetViewName(table);
                sourceToken = String.format("%1$s %2$s", tempDataViewName, table);
            } else {
                sourceToken = table;
            }
        }
        return hasLeadingBracket ? (LEADING_BRACKET + sourceToken) : sourceToken;
    }

    /**
     * Returns table name from previously cleaned from-expression
     * Removes temporary dataset view (table ending with string '_kpis') from the source expression.
     * Method works only in case of {@link Datasource#KPI_IN_MEMORY} and {@link Datasource#KPI_POST_AGGREGATION},
     * where only one datasource can exist, thus only two columns can be in the input
     *
     * @param cleanedFromExpression from-expression without data sources
     * @return updated source expression
     */
    public String getTableNameOnFromExpression(final String cleanedFromExpression) {
        final String[] splitOnWhiteSpace = PATTERN_TO_SPLIT_ON_WHITESPACE.split(getSourceExpression(cleanedFromExpression));

        String tempDataViewSuffix = KpiNameUtils.NAME_TOKEN_SEPARATOR + KpiNameUtils.TEMP_DATASET_VIEW_SUFFIX;
        if (splitOnWhiteSpace[0].endsWith(tempDataViewSuffix)) {
            return splitOnWhiteSpace[1];
        }
        return splitOnWhiteSpace[0];
    }

    public Set<Relation> collectComplexRelations(final String expression) {
        return sqlProcessorDelegator.getRelations(ComplexDefinitionExpression.of(expression));
    }

    public Set<Relation> collectOndemandRelations(final String expression) {
        return sqlProcessorDelegator.getRelations(OnDemandDefinitionExpression.of(expression));
    }

    public Set<Reference> collectComplexReferences(final String expression) {
        return sqlProcessorDelegator.getReferences(ComplexDefinitionExpression.of(expression));
    }

    public Set<Reference> collectOndemandReferences(final String expression) {
        return sqlProcessorDelegator.getReferences(OnDemandDefinitionExpression.of(expression));
    }

    private static Stream<TreeNode<? extends TreeNode<?>>> treeNodes(final LogicalPlan logicalPlan) {
        return Stream.concat(
                Stream.of(logicalPlan),
                SparkCollections.asJavaStream(logicalPlan.expressions()).flatMap(SparkTrees::asJavaStreamWithChildren)
        );
    }
}

