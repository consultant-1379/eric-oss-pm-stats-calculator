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

import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_ON_DB_DELIMITER;
import static com.ericsson.oss.air.pm.stats.calculator.api.constant.PatternConstants.PATTERN_TO_SPLIT_ON_DOT;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceFilters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTableFilters;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.mapper.SourceMapper;
import com.ericsson.oss.air.pm.stats.calculator.model.KpiDefinitionsByFilter;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Reference;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHierarchy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.parquet.Preconditions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KpiDefinitionHelperImpl {

    private static final Comparator<KpiDefinition> COMPARATOR_DATA_LOOK_BACK_LIMIT = Comparator.comparingInt(KpiDefinition::getDataLookbackLimit);

    private final KpiDefinitionHierarchy kpiDefinitionHierarchy;
    private final SqlProcessorDelegator sqlProcessorDelegator;
    private final SqlExpressionHelperImpl sqlExpressionHelper;

    public boolean areCustomFilterDefinitions(@NonNull final Collection<KpiDefinition> kpiDefinitions) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(kpiDefinitions), "kpiDefinitions is empty");

        return CollectionUtils.isNotEmpty(getFilterAssociatedKpiDefinitions(kpiDefinitions));
    }

    public Set<String> extractAliases(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream()
                             .map(KpiDefinition::getAlias)
                             .collect(Collectors.toSet());
    }

    public Set<Integer> extractAggregationPeriods(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream()
                             .map(KpiDefinition::getAggregationPeriod)
                             .filter(Objects::nonNull)
                             .map(Integer::valueOf)
                             .collect(Collectors.toSet());
    }

    public Integer extractAggregationPeriod(final Collection<? extends KpiDefinition> kpiDefinitions) {
        final Set<Integer> aggregationPeriods = extractAggregationPeriods(kpiDefinitions);

        Preconditions.checkArgument(aggregationPeriods.size() == 1, "KPI Definitions should contain one aggregation period only");

        return IterableUtils.first(aggregationPeriods);
    }

    public DatasourceTables extractNonInMemoryDatasourceTables(@NonNull final Collection<? extends KpiDefinition> kpis) {
        final DatasourceTables sourceTablesByDatasource = DatasourceTables.newInstance();
        final Stream<SourceTable> nonInMemorySourceTables = kpis.stream()
                .map(this::getSourceTables)
                .flatMap(Collection::stream)
                .filter(SourceTable::isNonInMemory);

        nonInMemorySourceTables.forEach(sourceTablesByDatasource::computeIfAbsent);

        return sourceTablesByDatasource;
    }

    public Set<Datasource> extractNonInMemoryDataSources(@NonNull final Collection<? extends KpiDefinition> kpis) {
        return kpis.stream()
                .map(this::collectNonSimpleRelations)
                .flatMap(Collection::stream)
                .map(Relation::datasource)
                .flatMap(Optional::stream)
                .filter(Datasource::isNonInMemory)
                .collect(toSet());
    }

    public Set<SourceTable> getSourceTables(final KpiDefinition definition) {
        if (definition.isSimple()) {
            return Collections.emptySet();
        }
        return SourceMapper.toSourceTables(collectNonSimpleRelations(definition));
    }

    public Set<SourceColumn> getSourceColumns(final KpiDefinition definition) {
        if (definition.isSimple()) {
            return Collections.emptySet();
        }
        return SourceMapper.toSourceColumns(collectNonSimpleReferences(definition), collectNonSimpleRelations(definition));
    }

    private Set<Relation> collectNonSimpleRelations(final KpiDefinition definition) {
        if (definition.isSimple()) {
            return Collections.emptySet();
        }

        if (definition.isScheduled()) {
            return sqlExpressionHelper.collectComplexRelations(definition.getExpression());
        } else {
            return sqlExpressionHelper.collectOndemandRelations(definition.getExpression());
        }
    }

    private Set<Reference> collectNonSimpleReferences(final KpiDefinition definition) {
        if (definition.isScheduled()) {
            return sqlExpressionHelper.collectComplexReferences(definition.getExpression());
        } else {
            return sqlExpressionHelper.collectOndemandReferences(definition.getExpression());
        }
    }

    /**
     * Checks whether the KPI data sources contain the received {@link DatasourceType}.
     * If data source does not exist in {@link DatasourceRegistry} then it matches as having the same type.
     *
     * @param kpiDefinition
     *            KPI to be checked
     * @param datasourceType
     *            data source type to be compared with
     * @return true: if KPI data source contains received datasourceType or data source is NOT present in registry
     *         false: if KPI data source does NOT contain received datasourceType and all data source is present in registry
     */
    public boolean isKpiDataSourcesContainType(final KpiDefinition kpiDefinition, final DatasourceType datasourceType) {
        return getSourceTables(kpiDefinition).stream().anyMatch(
                source -> DatasourceRegistry.getInstance().datasourceHasType(source.getDatasource(), datasourceType.name())
                        .orElse(true));
    }

    /**
     * Retrieves the super-set of datasource tables for the given {@link Map} of KPIs. Filters out datasources of type 'kpi_inmemory'.
     *
     * @param kpiDefinitions
     *            the {@link Map} of {@link KpiDefinition}s keyed by stage
     * @return the {@link Set} of datasource tables
     */
    public Set<Table> getTablesFromStagedKpis(final Map<Integer, List<KpiDefinition>> kpiDefinitions) {
        return kpiDefinitions.values()
                .stream()
                .flatMap(Collection::stream)
                .map(this::getSourceTables)
                .flatMap(Collection::stream)
                .filter(source -> source.getDatasource().isNonInMemory())
                .map(SourceTable::getTable)
                .collect(toSet());
    }

    /**
     * Group the provided {@link Set} of {@link Filter}s by {@link Datasource} and {@link Table}.<br>
     * Detailed information:
     * <pre>{@code
     *  kpi_db://kpi_sector_60.TO_DATE(local_timestamp) = '${param.date_for_filter}'
     *  |====|  |============| |===================================================|
     *    ||          ||                               ||
     *    ||          ||                               |==== Filter
     *    ||          |===================================== Table
     *    |================================================= Datasource
     * }</pre>
     *
     * @param filters {@link Set} of {@link Filter}s to be processed.
     * @return {@link DatasourceTableFilters} containing filter group by DataSource and Filter.
     */
    public DatasourceTableFilters groupFilters(final Set<Filter> filters) {
        final DatasourceFilters filtersSplitOnDatasource1 = DatasourceFilters.newInstance();
        for (final Filter filter : filters) {
            final String[] filterSplit = PATTERN_TO_SPLIT_ON_DB_DELIMITER.split(filter.getName(), 2);
            final Datasource dataSource = Datasource.of(filterSplit[0]);
            final Filter whereExpression = new Filter(filterSplit[1]);
            filtersSplitOnDatasource1.computeIfAbsent(dataSource, v -> new ArrayList<>()).add(whereExpression);
        }
        final DatasourceFilters filtersSplitOnDatasource = filtersSplitOnDatasource1;
        final DatasourceTableFilters filterSplitByTableByDatasource = DatasourceTableFilters.newInstance(filtersSplitOnDatasource.size());
        for (final Map.Entry<Datasource, List<Filter>> filterByDatasource : filtersSplitOnDatasource.entrySet()) {
            final Map<Table, List<Filter>> filtersSplitOnTable = new HashMap<>();
            for (final Filter filter : filterByDatasource.getValue()) {
                final String[] tableSplit = PATTERN_TO_SPLIT_ON_DOT.split(filter.getName(), 2);
                final Table tableName = Table.of(tableSplit[0]);
                final Filter filterForTable = new Filter(tableSplit[1]);
                filtersSplitOnTable.computeIfAbsent(tableName, v -> new ArrayList<>()).add(filterForTable);
            }
            filterSplitByTableByDatasource.put(filterByDatasource.getKey(), filtersSplitOnTable);
        }
        return filterSplitByTableByDatasource;
    }

    public Set<Column> extractColumns(@NonNull final Collection<Filter> filters) {
        final Set<Reference> references = sqlProcessorDelegator.filters(filters);
        return references.stream().map(Reference::column).flatMap(Optional::stream).collect(toSet());
    }

    /**
     * Extract {@link Set} of {@link Column}s containing KPI Definition names for the provided alias and aggregation period.
     *
     * @param kpiDefinitions
     *         {@link Collection} of {@link KpiDefinition}s to extract {@link Column}s from.
     * @param alias
     *         alias to filter by.
     * @param aggregationPeriodInMinutes
     *         aggregation period to filter by.
     * @return {@link Set} of {@link Column}s containing KPI Definition names
     */
    public Set<Column> extractKpiDefinitionNameColumns(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions,
                                                       final String alias,
                                                       final Integer aggregationPeriodInMinutes) {
        return kpiDefinitions.stream()
                             .filter(kpiDefinition -> kpiDefinition.getAlias().equals(alias))
                             .filter(kpiDefinition -> kpiDefinition.getAggregationPeriod().equals(aggregationPeriodInMinutes.toString()))
                             .map(KpiDefinition::getName)
                             .map(Column::of)
                             .collect(Collectors.toSet());
    }

    /**
     * Extract {@link List} of {@link Column}s containing aggregation element columns for the provided alias.
     *
     * @param alias alias to filter by.
     * @param kpiDefinitions {@link Collection} of {@link KpiDefinition}s to extract {@link Column}s from.
     * @return {@link List} of {@link Column}s containing aggregation element columns.
     */
    public List<Column> extractAggregationElementColumns(final String alias, @NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream()
                             .filter(kpiDefinition -> kpiDefinition.getAlias().equalsIgnoreCase(alias))
                             .map(kpiDefinition -> sqlProcessorDelegator.aggregationElements(kpiDefinition.getAggregationElements()))
                             .flatMap(Collection::stream)
                             .map(reference -> Column.of(reference.aliasOrColumnName()))
                             .collect(toList());
    }

    /**
     * Extracts column names from aggregation elements. It returns alias, removes
     * table name and removes duplicates.
     *
     * @param kpiDefinition
     *            {@link KpiDefinition} containing the aggregation elements to be extracted
     * @return A {@link Set} of extracted columns
     */
    public Set<String> extractAggregationElementColumns(final KpiDefinition kpiDefinition) {

        return sqlProcessorDelegator.aggregationElements(kpiDefinition.getAggregationElements()).stream()
                .map(Reference::aliasOrColumnName)
                .collect(toSet());
    }

    /**
     * Retrieves the super-set of aggregation elements for the given {@link List} of KPIs and alias.
     *
     * @param kpiDefinitions
     *            the {@link Collection} of {@link KpiDefinition}s to check
     * @param alias
     *            the alias of the KPI
     * @return the {@link List} of aggregation elements
     */
    public List<String> getKpiAggregationElementsForAlias(final Collection<KpiDefinition> kpiDefinitions, final String alias) {
        return kpiDefinitions
                .stream()
                .filter(kpi -> kpi.getAlias().equals(alias))
                .map(KpiDefinition::getAggregationElements)
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .collect(toList());
    }

    public Map<Integer, List<KpiDefinition>> groupByAggregationPeriod(final Collection<? extends KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream().collect(groupingBy(kpiDefinition -> Integer.valueOf(kpiDefinition.getAggregationPeriod())));
    }

    public Map<DataIdentifier, List<KpiDefinition>> groupByDataIdentifier(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream()
                             .filter(KpiDefinition::hasInputDataIdentifier)
                             .collect(Collectors.groupingBy(KpiDefinition::getInpDataIdentifier));
    }

    public KpiDefinitionsByFilter groupByFilter(@NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        final KpiDefinitionsByFilter result = KpiDefinitionsByFilter.newInstance();
        for (final KpiDefinition kpiDefinition : kpiDefinitions) {
            result.addKpiDefinitions(new HashSet<>(kpiDefinition.getFilter()), Set.of(kpiDefinition));
        }
        return result;
    }

    private Set<KpiDefinition> getSucceedingKpis(final Collection<KpiDefinition> kpiDefinitionsWithFilterAttribute) {
        final Set<KpiDefinition> kpiDefinitionsWithEventualFilter = new HashSet<>();
        for (final KpiDefinition kpiWithFilterAttribute : kpiDefinitionsWithFilterAttribute) {
            kpiDefinitionsWithEventualFilter
                .addAll(Optional.ofNullable(kpiDefinitionHierarchy.getKpiSuccessors().get(kpiWithFilterAttribute))
                    .orElse(Collections.emptySet()));
        }
        return kpiDefinitionsWithEventualFilter;
    }

    /**
     * Returns a {@link Set} of {@link KpiDefinition} which have a custom filter associated directly or indirectly.
     *
     * @param kpiDefinitions
     *            the {@link Collection} of {@link KpiDefinition}s to check
     * @return a {@link Set} of {@link KpiDefinition}s with filter
     */
    public Set<KpiDefinition> getFilterAssociatedKpiDefinitions(final Collection<KpiDefinition> kpiDefinitions) {
        final Set<KpiDefinition> kpiDefinitionsWithFilter = getKpiDefinitionsWithFilterAttribute(kpiDefinitions);
        kpiDefinitionsWithFilter.addAll(getSucceedingKpis(kpiDefinitionsWithFilter));
        return kpiDefinitionsWithFilter;
    }

    /**
     * Filters a {@link Collection} of {@link KpiDefinition} returning only those which have a custom filter attribute.
     *
     * @param kpiDefinitions
     *            the {@link Collection} of {@link KpiDefinition}s to check
     * @return a {@link List} of custom filter {@link KpiDefinition}s
     */
    private Set<KpiDefinition> getKpiDefinitionsWithFilterAttribute(final Collection<KpiDefinition> kpiDefinitions) {
        return kpiDefinitions.stream()
            .filter(kpi -> Objects.nonNull(kpi.getFilter()) && !kpi.getFilter().isEmpty())
            .collect(toSet());
    }

    private Set<Filter> getDistinctFilters(final Collection<KpiDefinition> kpiDefinitions) {
        final Set<Filter> filters = new HashSet<>();
        for (final KpiDefinition kpiDefinition : kpiDefinitions) {
            if (Objects.nonNull(kpiDefinition.getFilter())) {
                filters.addAll(kpiDefinition.getFilter());
            }
        }
        return filters;
    }

    private void mergeKpiGroup(final List<Set<KpiDefinition>> kpiGroupsMerged, final Set<KpiDefinition> kpiGroup) {
        final Set<Filter> currentKpiGroupFilter = getDistinctFilters(new ArrayList<>(kpiGroup));
        final Optional<Set<KpiDefinition>> nonConflictingKpiGroup = findKpiGroupWithNonConflictingFilters(kpiGroupsMerged, currentKpiGroupFilter);
        if (nonConflictingKpiGroup.isPresent()) {
            nonConflictingKpiGroup.get().addAll(kpiGroup);
        } else {
            kpiGroupsMerged.add(kpiGroup);
        }
    }

    private List<Set<KpiDefinition>> mergeKpiGroupsWithNonConflictingFilters(final List<Set<KpiDefinition>> kpiGroups) {
        final List<Set<KpiDefinition>> kpiGroupsMerged = new ArrayList<>();
        for (final Set<KpiDefinition> kpiGroup : kpiGroups) {
            mergeKpiGroup(kpiGroupsMerged, kpiGroup);
        }
        return kpiGroupsMerged;
    }

    private boolean areFiltersNonConflicting(final Set<Filter> filters, final Set<Filter> filtersInNonConflictingGroup) {
        for (final Filter filter1 : filters) {
            for (final Filter filter2 : filtersInNonConflictingGroup) {
                if (filter1.isConflicting(filter2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Optional<Set<KpiDefinition>> findKpiGroupWithNonConflictingFilters(final List<Set<KpiDefinition>> kpiGroupsMerged,
                                                                                      final Set<Filter> filters) {
        Set<KpiDefinition> nonConflictingKpiGroup = null;
        for (final Set<KpiDefinition> kpiGroup : kpiGroupsMerged) {
            final Set<Filter> filtersInNonConflictingGroup = getDistinctFilters(new ArrayList<>(kpiGroup));
            if (areFiltersNonConflicting(filters, filtersInNonConflictingGroup)) {
                nonConflictingKpiGroup = kpiGroup;
                break;
            }
        }
        return Optional.ofNullable(nonConflictingKpiGroup);
    }

    /**
     * Group {@link KpiDefinition}s which share same filters.
     *
     * @param kpiDefinitions
     *            the {@link Collection} of {@link KpiDefinition}s to check
     * @param kpiDefinitionHierarchy
     *            the {@link KpiDefinitionHierarchy} of {@link KpiDefinition}s
     * @return {@link Map} of {@link KpiDefinition} keyed by their filter
     */
    public KpiDefinitionsByFilter groupFilterAssociatedKpisByFilters(final Collection<KpiDefinition> kpiDefinitions,
                                                                            final KpiDefinitionHierarchy kpiDefinitionHierarchy) {
        final Set<KpiDefinition> kpisWithFilterAttribute = getKpiDefinitionsWithFilterAttribute(kpiDefinitions);
        final List<Set<KpiDefinition>> kpiGroups = groupKpisWithSucceedingKpis(kpisWithFilterAttribute, kpiDefinitionHierarchy);
        final List<Set<KpiDefinition>> kpiGroupsMerged = mergeKpiGroupsWithNonConflictingFilters(kpiGroups);

        final KpiDefinitionsByFilter kpisByFilters = KpiDefinitionsByFilter.newInstance();
        for (final Set<KpiDefinition> kpiGroup : kpiGroupsMerged) {
            kpisByFilters.addKpiDefinitions(getDistinctFilters(new ArrayList<>(kpiGroup)), kpiGroup);
        }
        return kpisByFilters;
    }

    public List<KpiDefinition> filterByAggregationPeriod(final int aggregationPeriod,
                                                         @NonNull final Collection<? extends KpiDefinition> kpiDefinitions) {
        final String aggregationPeriodString = String.valueOf(aggregationPeriod);

        return kpiDefinitions.stream()
                             .filter(kpiDefinition -> aggregationPeriodString.equals(kpiDefinition.getAggregationPeriod()))
                             .collect(Collectors.toList());
    }

    private List<Set<KpiDefinition>> groupKpisWithSucceedingKpis(final Collection<KpiDefinition> kpisWithFilterAttribute,
                                                                        final KpiDefinitionHierarchy kpiDefinitionHierarchy) {
        final List<Set<KpiDefinition>> kpiGroups = new ArrayList<>();
        for (final KpiDefinition kpiWithFilterAttribute : kpisWithFilterAttribute) {
            final Set<KpiDefinition> kpiGroup = new HashSet<>();
            kpiGroup.add(kpiWithFilterAttribute);
            kpiGroup.addAll(
                Optional.ofNullable(kpiDefinitionHierarchy.getKpiSuccessors().get(kpiWithFilterAttribute)).orElse(Collections.emptySet()));
            kpiGroup.addAll(Optional.ofNullable(kpiDefinitionHierarchy.getKpiSuccessors().get(kpiWithFilterAttribute))
                .orElse(Collections.emptySet()));
            kpiGroups.add(kpiGroup);
        }
        return kpiGroups;
    }

    public String extractExecutionGroup(@NonNull final Collection<? extends KpiDefinition> kpis) {
        final Set<String> executionGroups = kpis.stream().map(KpiDefinition::getExecutionGroup).collect(toSet());

        Preconditions.checkArgument(executionGroups.size() == 1, "KPI Definitions should only contain one execution group");

        return IterableUtils.first(executionGroups);
    }

    public Set<DataIdentifier> extractDataIdentifiers(@NonNull final Collection<? extends KpiDefinition> kpis) {
        return kpis.stream()
                .filter(KpiDefinition::hasInputDataIdentifier)
                .map(KpiDefinition::getInpDataIdentifier)
                .collect(toSet());
    }

    public Integer extractDataLookbackLimit(final @NonNull Collection<? extends KpiDefinition> kpis) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(kpis), "kpis is empty");
        return Collections.max(kpis, COMPARATOR_DATA_LOOK_BACK_LIMIT).getDataLookbackLimit();
    }

    public SchemaDetail getSchemaDetailBy(@NonNull final Collection<? extends KpiDefinition> kpis, final DataIdentifier dataIdentifier) {
        final Set<SchemaDetail> schemaDetails = kpis.stream()
                    .filter(kpiDefinition -> kpiDefinition.hasSameDataIdentifier(dataIdentifier))
                    .map(KpiDefinition::getSchemaDetail)
                    .collect(toSet());

        Preconditions.checkArgument(schemaDetails.size() == 1, "KPI definitions should only have a single Schema Detail for the same data identifier");

        return IterableUtils.first(schemaDetails);
    }

    public Set<SchemaDetail> extractSchemaDetails(@NonNull final Collection<? extends KpiDefinition> kpis) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(kpis), "kpis is empty");
        return kpis.stream().map(KpiDefinition::getSchemaDetail).collect(toSet());
    }

    /**
     * Returns staged {@link KpiDefinition}s for the given aggregation period.
     *
     * @param kpiDefinitions the {@link Collection} of {@link KpiDefinition}s to check
     * @param aggregationPeriodInMinutes the aggregation period to be filtered
     * @return a {@link Map} of KPIs filtered by aggregation period with key as stage number and value as {@link List} of {@link KpiDefinition}s for
     *         that stage
     */
    public Map<Integer, List<KpiDefinition>> getStagedKpisForAggregationPeriod(final Collection<KpiDefinition> kpiDefinitions,
                                                                                      final int aggregationPeriodInMinutes) {

        final Map<Integer, List<KpiDefinition>> groupedKpis = groupKpisByStage(kpiDefinitions);

        final Map<Integer, List<KpiDefinition>> filteredKpisByStage = new HashMap<>(groupedKpis.size());

        for (final Map.Entry<Integer, List<KpiDefinition>> kpiEntry : groupedKpis.entrySet()) {
            final List<KpiDefinition> filteredKpis = kpiEntry.getValue().stream()
                    .filter(kpi -> Objects.nonNull(kpi.getAggregationPeriod()))
                    .filter(kpi -> kpi.getAggregationPeriod().equals(String.valueOf(aggregationPeriodInMinutes)))
                    .collect(toList());

            if (!filteredKpis.isEmpty()) {
                filteredKpisByStage.put(kpiEntry.getKey(), filteredKpis);
            }
        }

        return filteredKpisByStage;
    }

    /**
     * Groups KPIs by stages.
     *
     * @param kpiDefinitions
     *            a {@link List} of every KPI
     * @return a {@link Map} of stages and corresponding KPIs.
     */
    public Map<Integer, List<KpiDefinition>> groupKpisByStage(final Collection<KpiDefinition> kpiDefinitions) {
        final Map<Integer, List<KpiDefinition>> definitionsByStage = new HashMap<>();
        int stage = 1;

        List<KpiDefinition> definitionsToGroup = new ArrayList<>(kpiDefinitions);
        while (!definitionsToGroup.isEmpty()) {
            definitionsToGroup = groupDefinitions(definitionsToGroup, definitionsByStage, stage);
            stage++;
        }

        return Collections.unmodifiableMap(definitionsByStage);
    }

    // Returns the KPIs conflicts only, list gets smaller each iteration (ideally, still need to handle bad model!)
    private List<KpiDefinition> groupDefinitions(final Collection<KpiDefinition> definitions,
                                                        final Map<Integer, List<KpiDefinition>> definitionsByStage, final int stage) {
        final List<KpiDefinition> definitionsToParse = new ArrayList<>();
        definitionsByStage.put(stage, new ArrayList<>());

        for (final KpiDefinition kpiDefinition : definitions) {
            final Set<String> dependencies = findDependencies(kpiDefinition, definitions);

            if (dependencies.isEmpty()) {
                definitionsByStage.get(stage).add(kpiDefinition);
            } else {
                definitionsToParse.add(kpiDefinition);
            }
        }

        return definitionsToParse;
    }

    /**
     * Finds the dependencies of a particular KPI from a list of other definitions.
     *
     * @param currentDefinition {@link KpiDefinition} to find the dependencies for
     * @param otherDefinitions {@link Collection} of {@link KpiDefinition}s to get dependencies from
     * @return names of the KPI dependencies
     */
    private Set<String> findDependencies(final KpiDefinition currentDefinition, final Collection<KpiDefinition> otherDefinitions) {
        final Set<String> expressionColumns = getSourceColumns(currentDefinition).stream()
                .map(SourceColumn::getColumn)
                .collect(toSet());
        final Set<String> currentDefinitionColumnsAndAggregationElements = new HashSet<>();
        currentDefinitionColumnsAndAggregationElements.addAll(expressionColumns);

        //TODO review whether we need to add aggregation element columns
        final Set<String> aggregationElementColumns = extractAggregationElementColumns(currentDefinition);
        currentDefinitionColumnsAndAggregationElements.addAll(aggregationElementColumns);

        final Set<String> kpiNames = otherDefinitions.stream()
                .map(KpiDefinition::getName)
                .collect(toSet());
        kpiNames.remove(currentDefinition.getName());
        kpiNames.retainAll(currentDefinitionColumnsAndAggregationElements);
        return kpiNames;
    }
}
