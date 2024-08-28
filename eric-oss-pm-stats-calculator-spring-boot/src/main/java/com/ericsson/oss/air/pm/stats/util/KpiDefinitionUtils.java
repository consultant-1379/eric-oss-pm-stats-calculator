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

import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_ON_DOT;
import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_ON_WHITESPACE;
import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_WHITESPACE_AND_DOT;
import static com.ericsson.oss.air.pm.stats.calculator.api.model.KpiAggregationType.FIRST;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.MAX_UNION_AGGREGATION_TYPE;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.PERCENTILE_INDEX_80_UDAF;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.PERCENTILE_INDEX_90_UDAF;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.Definition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for parsing and filtering {@link KpiDefinition} instances.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiDefinitionUtils {

    public static final String DEFINITION_NAME_KEY = "name";
    public static final String DEFINITION_ALIAS_KEY = "alias";
    public static final String DEFINITION_EXPRESSION_KEY = "expression";
    public static final String DEFINITION_OBJECT_TYPE_KEY = "object_type";
    public static final String DEFINITION_AGGREGATION_TYPE_KEY = "aggregation_type";
    public static final String DEFINITION_AGGREGATION_PERIOD_KEY = "aggregation_period";
    public static final String DEFINITION_EXPORTABLE_KEY = "exportable";
    public static final String DEFINITION_FILTER_KEY = "filter";
    public static final String DEFINITION_AGGREGATION_ELEMENTS_KEY = "aggregation_elements";
    public static final String DEFINITION_IDENTIFIER_KEY = "inp_data_identifier";
    public static final String DEFINITION_SCHEMA_DATA_SPACE_KEY = "schema_data_space";
    public static final String DEFINITION_SCHEMA_CATEGORY_KEY = "schema_category";
    public static final String DEFINITION_SCHEMA_NAME_KEY = "schema_name";
    public static final String DEFINITION_EXECUTION_GROUP_KEY = "execution_group";
    public static final String DEFINITION_RELIABILITY_OFFSET_KEY = "data_reliability_offset";
    public static final String DEFINITION_LOOKBACK_LIMIT_KEY = "data_lookback_limit";
    public static final String DEFINITION_SCHEMA_DETAIL = "schema_detail";
    public static final String DEFINITION_REEXPORT_LATE_DATA = "reexport_late_data";
    private static final Map<String, String> UDAF_TO_AGGREGATION_FUNCTION_MAP;
    private static final Pattern PATTERN_TO_FIND_DIMENSION_TABLE_NAME = Pattern.compile("tabular_parameters://([A-Za-z0-9]+(_[A-Za-z0-9]*)*)");

    static {
        UDAF_TO_AGGREGATION_FUNCTION_MAP = new HashMap<>();
        UDAF_TO_AGGREGATION_FUNCTION_MAP.put(PERCENTILE_INDEX_80_UDAF, MAX_UNION_AGGREGATION_TYPE);
        UDAF_TO_AGGREGATION_FUNCTION_MAP.put(PERCENTILE_INDEX_90_UDAF, MAX_UNION_AGGREGATION_TYPE);
    }

    /**
     * Builds an SQL expression for simple aggregation for the given KPI Definition in the form:
     *
     * <pre>
     *     &lt;aggregationType&gt;(&lt;kpiName&gt;) AS &lt;kpiName&gt;
     * </pre>
     * <p>
     * For example, for the KPI <code>simpleKpi</code> with an aggregation type of <b>SUM</b>:
     *
     * <pre>
     *     SUM(simpleKpi) AS simpleKpi
     * </pre>
     *
     * @param name            name of the KPI Definition
     * @param aggregationType aggregation type of the KPI Definition
     * @return the SQL expression
     */
    public static String makeKpiNameAsSql(final String name, final String aggregationType) {
        final String unionAggregationType = getUnionAggregationType(aggregationType);
        if (FIRST.getJsonType().equalsIgnoreCase(unionAggregationType)) {
            return String.format("%1$s(%2$s, true) AS %2$s", unionAggregationType, name);
        }
        return String.format("%1$s(%2$s) AS %2$s", unionAggregationType, name);
    }

    /**
     * Converts a list of {@link KpiDefinition}s to a {@link Set} of generic {@link Definition} objects.
     *
     * @param payloadAsKpiDefinitionObjects the {@link Collection} of {@link KpiDefinition} to be converted
     * @return a {@link Set} of {@link Definition}s
     */
    public static Set<Definition> convertKpiDefinitionsToRawDefinitionSet(final Collection<KpiDefinition> payloadAsKpiDefinitionObjects) {
        final Set<Definition> rawKpiDefinitions = new HashSet<>(payloadAsKpiDefinitionObjects.size());

        for (final KpiDefinition kpiDefinition : payloadAsKpiDefinitionObjects) {
            final Map<String, Object> kpiAttributes = new HashMap<>(10);
            kpiAttributes.put(DEFINITION_NAME_KEY, kpiDefinition.getName());
            kpiAttributes.put(DEFINITION_ALIAS_KEY, kpiDefinition.getAlias());
            kpiAttributes.put(DEFINITION_EXPRESSION_KEY, kpiDefinition.getExpression());
            kpiAttributes.put(DEFINITION_OBJECT_TYPE_KEY, kpiDefinition.getObjectType());
            kpiAttributes.put(DEFINITION_AGGREGATION_TYPE_KEY, kpiDefinition.getAggregationType());
            kpiAttributes.put(DEFINITION_AGGREGATION_PERIOD_KEY, kpiDefinition.getAggregationPeriod());
            kpiAttributes.put(DEFINITION_AGGREGATION_ELEMENTS_KEY, kpiDefinition.getAggregationElements());
            kpiAttributes.put(DEFINITION_FILTER_KEY, kpiDefinition.getFilter());
            kpiAttributes.put(DEFINITION_EXPORTABLE_KEY, kpiDefinition.getExportable());
            kpiAttributes.put(DEFINITION_IDENTIFIER_KEY, kpiDefinition.getInpDataIdentifier());
            kpiAttributes.put(DEFINITION_EXECUTION_GROUP_KEY, kpiDefinition.getExecutionGroup());
            kpiAttributes.put(DEFINITION_RELIABILITY_OFFSET_KEY, kpiDefinition.getDataReliabilityOffset());
            kpiAttributes.put(DEFINITION_LOOKBACK_LIMIT_KEY, kpiDefinition.getDataLookbackLimit());
            kpiAttributes.put(DEFINITION_SCHEMA_DETAIL, kpiDefinition.getSchemaDetail());
            rawKpiDefinitions.add(new Definition(kpiAttributes));
        }
        return rawKpiDefinitions;
    }

    /**
     * This method converts a {@link Collection} of {@link Definition}s back to their equivalent {@link Set} of {@link KpiDefinition}.
     *
     * @param definitions the {@link Collection} of {@link Definition}s to be converted to raw form
     * @return a {@link Set} of {@link KpiDefinition} objects
     */
    public static Set<KpiDefinition> convertDefinitionsToKpiDefinitions(final Collection<Definition> definitions) {
        final Set<KpiDefinition> kpiDefinitions = new HashSet<>();

        for (final Definition definition : definitions) {
            final DataIdentifier dataIdentifier = getDataIdentifierFromAttributes(definition);
            KpiDefinition kpiDefinition = KpiDefinition.builder()
                    .withName(definition.getAttributeByName(DEFINITION_NAME_KEY).toString())
                    .withAlias(definition.getAttributeByName(DEFINITION_ALIAS_KEY).toString())
                    .withExpression(definition.getAttributeByName(DEFINITION_EXPRESSION_KEY).toString())
                    .withObjectType(definition.getAttributeByName(DEFINITION_OBJECT_TYPE_KEY).toString())
                    .withAggregationType(definition.getAttributeByName(DEFINITION_AGGREGATION_TYPE_KEY).toString())
                    .withAggregationPeriod(String.valueOf(definition.getAttributeByName(DEFINITION_AGGREGATION_PERIOD_KEY)))
                    .withAggregationElements((List<String>) definition.getAttributeByName(DEFINITION_AGGREGATION_ELEMENTS_KEY))
                    .withExportable((Boolean) definition.getAttributeByName(DEFINITION_EXPORTABLE_KEY))
                    .withFilter((List<Filter>) definition.getAttributeByName(DEFINITION_FILTER_KEY))
                    .withInpDataIdentifier(dataIdentifier)
                    .withExecutionGroup(definitionAttributeToString(definition, DEFINITION_EXECUTION_GROUP_KEY))
                    .withDataReliabilityOffset((Integer) definition.getAttributeByName(DEFINITION_RELIABILITY_OFFSET_KEY))
                    .withDataLookbackLimit((Integer) definition.getAttributeByName(DEFINITION_LOOKBACK_LIMIT_KEY))
                    .withSchemaDetail(definition.getSchemaDetail())
                    .build();
            kpiDefinitions.add(kpiDefinition);
        }
        return kpiDefinitions;
    }

    private static String definitionAttributeToString(final Definition definition, final String attributeByName) {
        return definition.getAttributeByName(attributeByName) == null ? null : String.valueOf(definition.getAttributeByName(attributeByName));
    }

    private static String getUnionAggregationType(final String aggregationType) {
        final String aggregationTypeRaw = aggregationType;
        if (UDAF_TO_AGGREGATION_FUNCTION_MAP.containsKey(aggregationTypeRaw)) {
            return UDAF_TO_AGGREGATION_FUNCTION_MAP.get(aggregationTypeRaw);
        }
        return aggregationTypeRaw;
    }

    /**
     * Returns {@code true} if the {@code aggregationPeriod} is the default value of {@code -1} or {@code null}, otherwise {@code false}.
     *
     * @param aggregationPeriod The period to check
     * @return {@code true} if the {@code aggregationPeriod} is the default value, otherwise {@code false}
     */
    public static boolean isDefaultAggregationPeriod(final Object aggregationPeriod) {
        if (Objects.isNull(aggregationPeriod)) {
            return true;
        }
        return DEFAULT_AGGREGATION_PERIOD.equals(aggregationPeriod.toString());
    }

    /**
     * Returns a {@code Set} of aggregation element columns from the {@code aggregationElementsObject} provided.
     *
     * @param aggregationElementsObject containing aggregation elements
     * @return a {@code Set} of aggregation element columns
     */
    public static Set<String> getAggregationElements(final Object aggregationElementsObject) {
        if (Objects.isNull(aggregationElementsObject)) {
            return Collections.emptySet();
        }
        final List<String> aggregationElementsList = (List<String>) aggregationElementsObject;
        return aggregationElementsList.stream()
                .map(aggregationElement -> getValueFromPattern(PATTERN_TO_SPLIT_ON_DOT, aggregationElement))
                .map(aggregationElement -> getValueFromPattern(PATTERN_TO_SPLIT_ON_WHITESPACE, aggregationElement))
                .map(String::trim)
                .collect(toSet());
    }

    /**
     * Returns a {@code Set} of aggregation element tables from the {@code aggregationElementsObject} provided.
     *
     * @param aggregationElements containing aggregation elements
     * @return a {@code Set} of aggregation element tables
     */
    public static Set<String> getAggregationDbTables(final List<String> aggregationElements) {
        return Objects.isNull(aggregationElements) ? Collections.emptySet() :
                aggregationElements.stream()
                        .filter(aggregationElement -> aggregationElement.startsWith("kpi_"))
                        .map(aggregationElement -> getKeyFromPattern(PATTERN_TO_SPLIT_ON_DOT, aggregationElement))
                        .map(String::trim)
                        .collect(toSet());
    }

    public static Map<String, KpiDataType> getDefaultElementsAndTypes(final Collection<Definition> definitions) {
        Map<String, KpiDataType> defaultElementTypes = new HashMap<>();

        for (final Definition definition : definitions) {
            final List<AggregationElement> aggregationElements = definition.getAggregationElements().stream()
                    .map(KpiDefinitionUtils::getAggregationElement).collect(Collectors.toList());
            final List<AggregationElement> parametricElements = aggregationElements.stream().filter(AggregationElement::getIsParametric).collect(Collectors.toList());

            parametricElements.forEach(aggregationElement -> {
                defaultElementTypes.put(aggregationElement.getTargetColumn(), KpiDataType.POSTGRES_STRING);
                aggregationElements.remove(aggregationElement);
            });

            final boolean kpiSource = Arrays.stream(PATTERN_TO_SPLIT_ON_WHITESPACE.split(definition.getExpression()))
                    .filter(SourceTable::isValidSource)
                    .map(SourceTable::new)
                    .allMatch(SourceTable::isInternal);

            if (!kpiSource) {
                aggregationElements.forEach(aggregationElement -> defaultElementTypes.put(aggregationElement.getTargetColumn(), KpiDataType.POSTGRES_LONG));
            }
        }
        return defaultElementTypes;
    }

    public static List<AggregationElement> getAggregationElementsWithKpiSource(final Collection<Definition> definitions) {
        List<AggregationElement> aggregationElementsWithKpiSource = new ArrayList<>();

        for (Definition definition : definitions) {
            List<String> aggregationElements = definition.getAggregationElements();

            aggregationElementsWithKpiSource.addAll(
                    aggregationElements.stream()
                            .filter(aggregationElement -> aggregationElement.startsWith("kpi_"))
                            .map(KpiDefinitionUtils::getAggregationElement)
                            .collect(Collectors.toList()));
        }
        return aggregationElementsWithKpiSource;
    }

    private static AggregationElement getAggregationElement(final String element) {
        final String[] elements = PATTERN_TO_SPLIT_WHITESPACE_AND_DOT.split(element);
        final String sourceTableName = PATTERN_TO_SPLIT_ON_DOT.split(element)[0];
        final String sourceColumnName = elements.length == 1 ? element : elements[1];
        final String targetColumn = elements[elements.length - 1];
        final boolean isParametric = Pattern.compile("\\$\\{.*}").matcher(element).find();

        return AggregationElement.builder()
                .withExpression(element)
                .withSourceTable(sourceTableName)
                .withSourceColumn(sourceColumnName)
                .withTargetColumn(targetColumn)
                .withIsParametric(isParametric)
                .build();
    }

    private static String getValueFromPattern(final Pattern pattern, final String element) {
        final String[] elements = pattern.split(element);
        return elements.length == 1 ? element : elements[elements.length - 1];
    }

    private static String getKeyFromPattern(final Pattern pattern, final String element) {
        final String[] elements = pattern.split(element);
        return elements[0];
    }

    private static DataIdentifier getDataIdentifierFromAttributes(final Definition definition) {
        final DataIdentifier identifier = (DataIdentifier) definition.getAttributeByName(DEFINITION_IDENTIFIER_KEY);
        if (identifier != null) {
            return identifier;
        }
        final String dataSpace = (String) definition.getAttributeByName(DEFINITION_SCHEMA_DATA_SPACE_KEY);
        final String category = (String) definition.getAttributeByName(DEFINITION_SCHEMA_CATEGORY_KEY);
        final String schema = (String) definition.getAttributeByName(DEFINITION_SCHEMA_NAME_KEY);

        if (category == null || schema == null || dataSpace == null) {
            return null;
        }
        return DataIdentifier.of(dataSpace, category, schema);
    }

    public static List<KpiDefinition> changeTableNameIfTabularParameterIsPresent(final List<KpiDefinition> kpiDefinitionList,
                                                                                 final List<String> dimensionTableNames) {

        return kpiDefinitionList.stream()
                .map(kpiDefinition -> modifyKpiDefinitionIfContainsTabularParameter(kpiDefinition, dimensionTableNames))
                .collect(Collectors.toList());
    }

    private static String extractTabularParameterTableName(final String expression) {
        Matcher matcher = PATTERN_TO_FIND_DIMENSION_TABLE_NAME.matcher(expression);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("tabular parameter table name cannot be extracted");
    }

    private static KpiDefinition modifyKpiDefinitionIfContainsTabularParameter(final KpiDefinition kpiDefinition, final List<String> dimensionTableNames) {
        final String expression = kpiDefinition.getExpression();

        if (expression.contains(TABULAR_PARAMETERS.getName())) {
            final String tabularParameterTableName = extractTabularParameterTableName(expression);
            for (final String dimensionTableName : dimensionTableNames) {
                if (dimensionTableName.contains(tabularParameterTableName) && tabularParameterTableName.equals(dimensionTableName.substring(0, dimensionTableName.lastIndexOf('_')))) {
                    final List<String> modifiedAggregationElements = modifyAggregationElements(tabularParameterTableName, dimensionTableName, kpiDefinition.getAggregationElements());
                    final List<Filter> modifiedFilters = modifyFilters(tabularParameterTableName, dimensionTableName, kpiDefinition.getFilter());

                    kpiDefinition.setExpression(expression.replaceAll(tabularParameterTableName, dimensionTableName));
                    kpiDefinition.setAggregationElements(modifiedAggregationElements);
                    kpiDefinition.setFilter(modifiedFilters);
                    return kpiDefinition;
                }
            }
        }
        return kpiDefinition;
    }

    private static List<String> modifyAggregationElements(final String tabularParameterTableName,
                                                          final String dimensionTableName,
                                                          final List<String> aggregationElements) {

        return aggregationElements.stream()
                .map(element -> element.replaceAll(tabularParameterTableName, dimensionTableName))
                .collect(Collectors.toList());
    }

    private static List<Filter> modifyFilters(final String tabularParameterTableName,
                                              final String dimensionTableName,
                                              final List<Filter> filters) {
        return filters.stream()
                .map(Filter::getName)
                .map(filter -> filter.replaceAll(tabularParameterTableName, dimensionTableName))
                .map(Filter::new)
                .collect(Collectors.toList());
    }
}