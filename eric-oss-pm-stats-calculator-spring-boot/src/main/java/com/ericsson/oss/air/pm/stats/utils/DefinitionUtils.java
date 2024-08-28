/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.utils;

import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_ON_DOT;
import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_ON_WHITESPACE;
import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_WHITESPACE_AND_DOT;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static kpi.model.util.PatternConstants.PATTERN_AGGREGATION_ELEMENT_UDF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.AggregationElement;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionUtils {

    public static Map<String, KpiDataType> getDefaultElementsAndTypes(final Collection<KpiDefinitionEntity> definitions) {
        final Map<String, KpiDataType> defaultElementTypes = new HashMap<>();

        for (final KpiDefinitionEntity definition : definitions) {
            final List<AggregationElement> aggregationElements = definition.aggregationElements().stream()
                    .map(DefinitionUtils::getAggregationElement).collect(Collectors.toList());
            final List<AggregationElement> parametricElements = aggregationElements.stream().filter(AggregationElement::getIsParametric).collect(Collectors.toList());
            final List<AggregationElement> sparkUdfElements = aggregationElements
                    .stream()
                    .filter(agg -> PATTERN_AGGREGATION_ELEMENT_UDF.matcher(agg.getExpression()).find())
                    .collect(Collectors.toList());

            parametricElements.forEach(aggregationElement -> {
                defaultElementTypes.put(aggregationElement.getTargetColumn(), KpiDataType.POSTGRES_STRING);
                aggregationElements.remove(aggregationElement);
            });

            sparkUdfElements.forEach(aggregationElement -> {
                defaultElementTypes.put(aggregationElement.getTargetColumn(), KpiDataType.POSTGRES_UNLIMITED_STRING);
                aggregationElements.remove(aggregationElement);
            });

            final boolean kpiSource = Arrays.stream(PATTERN_TO_SPLIT_ON_WHITESPACE.split(definition.expression()))
                    .filter(SourceTable::isValidSource)
                    .map(SourceTable::new)
                    .allMatch(SourceTable::isInternal);

            if (!kpiSource) {
                aggregationElements.forEach(aggregationElement -> defaultElementTypes.put(aggregationElement.getTargetColumn(), KpiDataType.POSTGRES_LONG));
            }
        }
        return defaultElementTypes;
    }

    public static List<AggregationElement> getAggregationElementsWithKpiSource(final Collection<KpiDefinitionEntity> definitions, final Set<String> simpleElements) {
        final List<AggregationElement> aggregationElementsWithKpiSource = new ArrayList<>();

        for (final KpiDefinitionEntity definition : definitions) {
            final List<String> aggregationElements = definition.aggregationElements();

        /*
        If Complex depends on a Simple which is in the same table the table itself will only exist if the Simple and Complex definitions came in a different payload,
        if they came in the same payload we need to filter out the aggregation elements which types are coming from the Simple schema.
         */
            aggregationElementsWithKpiSource.addAll(
                    aggregationElements.stream()
                            .filter(aggregationElement -> aggregationElement.startsWith("kpi_"))
                            .map(DefinitionUtils::getAggregationElement)
                            .filter(aggregationElement -> !simpleElements.contains(aggregationElement.getTargetColumn()))
                            .collect(Collectors.toList()));
        }
        return aggregationElementsWithKpiSource;
    }

    public static List<AggregationElement> getAggregationElementsWithTabularParameterDataSource(final Collection<KpiDefinitionEntity> definitions) {
        return definitions.stream()
                .filter(definition -> definition.expression().contains(TABULAR_PARAMETERS.getName()))
                .map(KpiDefinitionEntity::aggregationElements)
                .flatMap(List::stream)
                .filter(aggregationElement -> !aggregationElement.startsWith("kpi_"))
                .map(DefinitionUtils::getAggregationElement)
                .filter(AggregationElement::isNotParametric)
                .collect(Collectors.toList());
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
}
