/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.api;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import kpi.model.KpiDefinitionRequest;
import org.apache.commons.lang3.tuple.Pair;

@Local
public interface KpiDefinitionService {

    /**
     * Persists or updates {@link Collection} of {@link KpiDefinitionRequest}s.
     *
     * @param definitions {@link KpiDefinitionRequest}s to be persisted or updated.
     */
    void upsert(KpiDefinitionRequest definitions);

    /**
     * Persists {@link KpiDefinitionEntity}s from the given {@link KpiDefinitionRequest} payload.
     *
     * @param definitions {@link KpiDefinitionRequest} to be persisted.
     */
    void insert(KpiDefinitionRequest definitions, UUID collectionId);

    /**
     * Updates a {@link KpiDefinitionEntity}.
     *
     * @param kpiDefinitionEntity {@link KpiDefinitionEntity} to be updated.
     */
    void updateWithoutColumnTypeChange(KpiDefinitionEntity kpiDefinitionEntity, UUID collectionId);

    /**
     * Updates a {@link KpiDefinitionEntity} and updates the column type of KPI output table.
     *
     * @param kpiDefinitionEntity {@link KpiDefinitionEntity} to be updated.
     */
    void updateWithColumnTypeChange(KpiDefinitionEntity kpiDefinitionEntity, UUID collectionId);

    /**
     * Sets the time_deleted column to the current timestamp for a list of KpiDefinitions.
     *
     * @param kpiDefinitionNames {@link List} of {@link String} KpiDefinitionNames to update the time_deleted column for.
     */
    void softDelete(List<String> kpiDefinitionNames, UUID collectionId);

    /**
     * Finds all persisted KPI Definitions.
     *
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findAll();

    /**
     * Finds all persisted KPI Definitions by collectionID
     *
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findAll(UUID collectionId);

    /**
     * Finds all persisted KPI Definitions, including soft deleted definitions.
     *
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findAllIncludingSoftDeleted();

    /**
     * Finds all persisted KPI Definitions, including soft deleted definitions by collectionId
     *
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findAllIncludingSoftDeleted(UUID collectionId);

    /**
     * Finds persisted Complex KPI Definitions.
     *
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findComplexKpis();

    /**
     * Finds all <strong>Simple</strong> execution groups
     *
     * @return {@link Set} of <strong>Simple</strong> execution groups
     */
    Set<String> findAllSimpleExecutionGroups();

    /**
     * Finds all KPI Names including KPIs that are soft-deleted.
     *
     * @return {@link Set} of names of KPIs
     */
    Set<String> findAllKpiNames();

    /**
     * Finds all KPI Names including KPIs that are soft-deleted by collectionId
     *
     * @return {@link Set} of names of KPIs
     */
    Set<String> findAllKpiNames(UUID collectionId);

    /**
     * Finds all <strong>Complex</strong> execution groups
     *
     * @return {@link Set} of <strong>Complex</strong> execution groups
     */
    Set<String> findAllComplexExecutionGroups();

    /**
     * Finds all Simple KPI Definition names (data input category is not empty).
     *
     * @return {@link Map} containing the {@link List} of KPI Definition names grouped by executionGroup.
     * @implNote The data input category has default value of empty String.
     */
    Map<String, List<String>> findAllSimpleKpiNamesGroupedByExecGroups();

    /**
     * Finds all <strong>Complex</strong> KPIs.
     * <br>
     * A KPI Definition considered to be Complex if the execution group is "COMPLEX".
     *
     * @return {@link Map} containing the {@link List} of KPI Definition names grouped by executionGroup.
     * @implNote The data input category has default value of empty String.
     */
    Map<String, List<String>> findAllComplexKpiNamesGroupedByExecGroups();

    /**
     * Finds all <strong>OnDemand</strong> alias and aggregation periods.
     * <br>
     * A KPI Definition is considered to be OnDemand if the input data identifier and execution group are empty
     *
     * @return {@link Set} containing the {@link Pair} of alias and aggregation periods.
     */
    Set<Pair<String, Integer>> findOnDemandAliasAndAggregationPeriods();

    /**
     * Finds all <strong>Scheduled</strong> alias and aggregation periods.
     * <br>
     * A KPI Definition is considered to be Scheduled if the execution group is not empty
     *
     * @return {@link Set} containing the {@link Pair} of alias and aggregation periods.
     */
    Set<Pair<String, Integer>> findScheduledAliasAndAggregationPeriods();

    /**
     * Finds all the kpis that are in the same table the kpiDefinition.
     *
     * @param kpiDefinition The definition, which we base the query on
     * @return {@link List} of the names of the above-mentioned kpis
     */
    List<String> findAllKpiNamesFromSameTableAsKpiDefinition(KpiDefinitionEntity kpiDefinition);

    /**
     * Returns if there are KPIs left, that belong to the given table.
     *
     * @param connection {@link Connection} to the database
     * @param table      {@link Table} table object
     * @return boolean true, if any KPI found false otherwise
     */
    boolean doesTableHaveAnyKpisLeft(Connection connection, Table table);

    /**
     * Finds all kpi definitions, related to the given execution group.
     *
     * @return {@link List} containing the {@link KpiDefinitionEntity} objects by executionGroup.
     */
    List<KpiDefinitionEntity> findKpiDefinitionsByExecutionGroup(String executionGroup);

    /**
     * Finds all kpi definitions related to the given execution group, and maps them to the needed format.
     * <br>
     * <strong>NOTE</strong>: Implementation does not eliminate duplicates.
     *
     * @param executionGroup the execution group to find definitions by
     * @param mapper         {@link Function} that maps {@link KpiDefinitionEntity} to the wanted output type
     * @return {@link Collection} containing specific output model defined by the mapper
     */
    <O> Collection<O> findKpiDefinitionsByExecutionGroup(String executionGroup, Function<? super KpiDefinitionEntity, ? extends O> mapper);

    /**
     * Find all KpiDefinition names by the provided execution group.
     *
     * @param executionGroup the execution group to find definition names by
     * @return {@link Collection} containing KpiDefinition names
     */
    Collection<String> findKpiDefinitionNamesByExecutionGroup(String executionGroup);

    /**
     * Finds all on demand kpi definitions, related to the given calculation id.
     *
     * @return {@link List} containing the on demand {@link KpiDefinitionEntity} objects by calculationID.
     */
    List<KpiDefinitionEntity> findOnDemandKpiDefinitionsByCalculationId(UUID calculationId);


    /**
     * Finds a single {@link KpiDefinitionEntity} by name.
     *
     * @param name the name to find the definition by.
     * @return the {@link KpiDefinitionEntity} matching the provided name.
     */
    KpiDefinitionEntity forceFindByName(String name, UUID collectionID);

    /**
     * Finds a {@link List} of {@link KpiDefinitionEntity} by names.
     *
     * @param names to find the definitions by.
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findByNames(Set<String> names);

    /**
     * Checks if <strong>kp_definition</strong> table contains data.
     *
     * @return true if <strong>kp_definition</strong> contains data, else false.
     */
    boolean doesContainData();

    /**
     * Saves the relations between on demand kpis and calculation.
     *
     * @param kpiNames      a {@link Set} of the names of the on demand kpis in the calculation
     * @param calculationId the unique IDs of the calculation
     */
    void saveOnDemandCalculationRelation(Set<String> kpiNames, UUID calculationId);

    /**
     * Counts the number of complex KPI definitions.
     *
     * @return The number of complex KPI definitions.
     */
    long countComplexKpi();

    /**
     * Deletes KPI definitions in a collection
     *
     * @param connection The connection to the database
     * @param kpiNames   {@link Collection} of the KPI names should be deleted
     */
    void deleteKpiDefinitionsByName(Connection connection, Collection<String> kpiNames);
}
