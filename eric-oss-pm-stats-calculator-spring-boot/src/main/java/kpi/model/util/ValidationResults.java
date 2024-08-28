/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.util;

import static kpi.model.api.validation.ValidationResult.invalid;
import static kpi.model.api.validation.ValidationResult.valid;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.common.model.attribute.ExpressionAttribute;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.element.FilterElement;

import kpi.model.api.table.Table;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.api.AggregationPeriodAttribute;
import kpi.model.api.table.definition.api.AliasAttribute;
import kpi.model.api.table.definition.api.FiltersAttribute;
import kpi.model.api.validation.ValidationResult;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationResults {
    public static <T> ValidationResult valueIsRequired(final String name, final T value) {
        return Objects.isNull(value) ? invalid("'%s' has null value, but this attribute is \"required\", must not be null", name) : valid();
    }

    public static ValidationResult valueIsNotBlank(final String name, final String value) {
        return value.isBlank() ? invalid("'%s' value '%s' is blank, but this attribute is \"required\", must not be empty", name, value) : valid();
    }

    public static ValidationResult aggregationElementsValueIsNotBlank(final String value) {
        return value.isBlank() ? invalid("Table attribute '%s' is empty, but this attribute is \"required\", must not be empty", value) : valid();
    }

    public static ValidationResult valueIsNotParameterized(final String name, final String value) {
        return PatternConstants.PATTERN_TO_FIND_PARAMETER_TOKEN.matcher(value).find()
                ? invalid("The attribute '%s' does not support parameters", name)
                : valid();
    }

    public static ValidationResult paramValueContainsOnlyEndOrStartTime(final String name, final String value) {
        Matcher matcher = PatternConstants.PATTERN_TO_FIND_PARAMETER_TOKEN.matcher(value);
        while (matcher.find()) {
            String match = matcher.group();
            if (!match.equals("${param.start_date_time}") && !match.equals("${param.end_date_time}")) {
                return invalid("The attribute '%s' only support '${param.start_date_time}' or '${param.end_date_time}' as parameter", name);
            }
        }
        return valid();
    }

    public static ValidationResult valueDoesNotContain(final String name, final String value, final String keyword) {
        return StringUtils.containsIgnoreCase(value, keyword) ? invalid("'%s' value '%s' cannot contain '%s'", name, value, keyword) : valid();
    }

    public static ValidationResult valueContainsExactlyOneFrom(final String name, final String value) {
        return PatternConstants.PATTERN_TO_FIND_EXACTLY_ONE_FROM.matcher(value).find()
                ? valid()
                : invalid("'%s' value '%s' must contain exactly one 'FROM'", name, value);
    }

    public static ValidationResult valueMatchesPattern(final String name, final String value, final Pattern pattern) {
        return pattern.matcher(value).find() ? valid() : invalid("'%s' value '%s' has invalid format. Format must follow the \"%s\" pattern", name, value, pattern.toString());
    }

    public static ValidationResult valueIsGreaterThanZero(final String name, final Integer value) {
        return value > 0 ? valid() : invalid("'%s' value '%s' must be greater than 0", name, value);
    }

    public static ValidationResult valueNotContainsOtherDatasourceWhenContains(Datasource actualDatasource, final String name, final String value) {
        if (!valueContainsDatasource(value, actualDatasource)) {
            return valid();
        }

        for (final DatasourceType datasourceType : DatasourceType.values()) {
            final Datasource dataSource = datasourceType.asDataSource();
            if (!dataSource.equals(actualDatasource) && valueContainsDatasource(value, dataSource)) {
                return invalid("'%s' value '%s' contains %s:// datasource cannot contain %s:// datasource", name, value, actualDatasource.getName(), dataSource.getName());
            }
        }
        return valid();
    }

    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static ValidationResult validateFiltersAreEmptyWhenExpressionContainsPostAggregationOrInMemoryDatasource(final List<? extends KpiDefinition> value) {
        for (final KpiDefinition definition : value) {
            final FiltersAttribute<? extends FilterElement> filters = definition.filters();
            final ExpressionAttribute expression = definition.expression();
            if (expressionContainsPostAggregationOrInMemoryDatasource(expression.value()) && filters.isNotEmpty()) {
                return invalid("'filters' attribute must be empty when 'expression' attribute contains 'kpi_post_agg://' or 'kpi_inmemory://'");
            }
        }
        return valid();
    }

    public static void validateAggregationPeriodIsGreaterThanOrEqualToDataReliabilityOffset(
            final AggregationPeriodAttribute aggregationPeriod,
            @NonNull final Iterable<? extends KpiDefinition> kpiDefinitions) {
        for (final KpiDefinition definition : kpiDefinitions) {
            if (aggregationPeriod.isNotDefault() && definition.dataReliabilityOffset().value() > aggregationPeriod.value()) {
                throw new IllegalArgumentException(String.format(
                        "'%s' value '%s' must be smaller than '%s' value '%s'",
                        definition.dataReliabilityOffset().name(), definition.dataReliabilityOffset().value(),
                        aggregationPeriod.name(), aggregationPeriod.value()
                ));
            }
        }
    }

    public static void validateAliasesAreUniqueForAKpiType(
            final Iterable<? extends Table> tables) {
        Set<Pair<AliasAttribute, AggregationPeriodAttribute>> resultSet = new HashSet<>();
        for (final Table table : tables) {
            if (!resultSet.add(Pair.of(table.alias(), table.aggregationPeriod()))) {
                throw new IllegalArgumentException(String.format(
                        "'%s' and '%s' values must be unique for a KPI type. Received values: '%s' and '%s'",
                        table.alias().name(), table.aggregationPeriod().name(), table.alias().value(), table.aggregationPeriod().value()));
            }
        }
    }

    public static ValidationResult validateKpiDefinitionExist(final List<? extends KpiDefinition> value) {
        return ValidationResults.isEmpty(value)
                ? invalid("At least one kpi definition is required")
                : valid();
    }

    public static void validateAliasesAreUniqueForAllKpiTypes(final Set<Pair<AliasAttribute, AggregationPeriodAttribute>> onDemandOutputTableNames,
                                                              final Set<Pair<AliasAttribute, AggregationPeriodAttribute>> scheduledOutputTableNames) {
        final Set<Pair<AliasAttribute, AggregationPeriodAttribute>> conflictingAliasAndAggPeriods = onDemandOutputTableNames.stream()
                .filter(scheduledOutputTableNames::contains)
                .collect(Collectors.toSet());
        if (!conflictingAliasAndAggPeriods.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "The following tables should only contain ON_DEMAND or SCHEDULED KPI types: '%s'",
                    conflictingAliasAndAggPeriods));
        }
    }

    public static Set<Pair<AliasAttribute, AggregationPeriodAttribute>> getOutputTableAliasAndAggPeriod(final List<? extends Table> kpiDefinitionOutputTables) {
        return kpiDefinitionOutputTables.stream().map(table -> Pair.of(table.alias(), table.aggregationPeriod())).collect(Collectors.toSet());
    }

    public static ValidationResult valueIsLessThanDefault(String name, Integer value, Integer defaultValue) {
        return value < defaultValue ? valid() : invalid("'%s' value '%s' must be less than default value '%s'", name, value, defaultValue);
    }

    private static boolean valueContainsDatasource(@NonNull final String value, @NonNull final Datasource datasource) {
        return value.contains(datasource.getName());
    }

    private static boolean expressionContainsPostAggregationOrInMemoryDatasource(String expression) {
        return valueContainsDatasource(expression, Datasource.KPI_POST_AGGREGATION) || valueContainsDatasource(expression, Datasource.KPI_IN_MEMORY);
    }
}
