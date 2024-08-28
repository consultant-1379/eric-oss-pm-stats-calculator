/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.ExecutionGroup;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.exception.EntityNotFoundException;

import org.apache.commons.lang3.tuple.Pair;

@Local
public interface KpiDefinitionRepository {

    /**
     * Counts the rows.
     *
     * @return The number of rows.
     */
    long count();

    /**
     * Counts the number of complex KPI definitions.
     *
     * @return The number of complex KPI definitions.
     */
    long countComplexKpi();

    /**
     * Finds all persisted KPI Definitions.
     *
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findAll();

    /**
     * Finds all persisted KPI Definitions by collectionId
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
     * Finds all persisted KPI Definitions, including soft deleted definitions with collectionId.
     *
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */

    List<KpiDefinitionEntity> findAllIncludingSoftDeleted(UUID collectionID);

    /**
     * Finds persisted Complex KPI Definitions.
     *
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findComplexKpis();

    /**
     * Finds a single {@link KpiDefinitionEntity} by name.
     *
     * @param name the name to find the definition by.
     * @return {@link Optional} wrapping the {@link KpiDefinitionEntity}, or {@link Optional#empty()} if KPI was not found by the provided name
     */
    Optional<KpiDefinitionEntity> findByName(String name, UUID collectionId);

    /**
     * Finds all KPI Name
     *
     * @return {@link Set} of names of KPIs
     */
    Set<String> findAllKpiNames();


    /**
     * Finds all KPI Name by collectionId
     *
     * @return {@link Set} of names of KPIs
     */
    Set<String> findAllKpiNames(UUID collectionId);

    /**
     * Finds a {@link List} of {@link KpiDefinitionEntity} by names.
     *
     * @param names to find the definitions by.
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findByNames(Set<String> names);

    /**
     * Finds all <strong>Simple</strong> execution groups
     *
     * @return {@link Set} of <strong>Simple</strong> execution groups
     */
    Set<String> findAllSimpleExecutionGroups();

    /**
     * Finds all <strong>Complex</strong> execution groups
     *
     * @return {@link Set} of <strong>Complex</strong> execution groups
     */
    Set<String> findAllComplexExecutionGroups();

    /**
     * Finds all <strong>Simple</strong> KPI Definition names.
     * <br>
     * A KPI Definition considered to be Simple if the data input category is not empty.
     *
     * @return {@link Map} containing the {@link List} of KPI Definition names grouped by execution group.
     * @implNote The data input category has default value of empty String.
     */
    Map<String, List<String>> findAllSimpleKpiNamesGroupedByExecGroups();

    /**
     * Finds all <strong>Complex</strong> KPI Definitions.
     * <br>
     * A KPI Definition considered to be Complex if the execution group is 'COMPLEX'.
     *
     * @return {@link Map} containing the {@link List} of KPI Definition names grouped by execution group.
     */
    Map<String, List<String>> findAllComplexKpiNamesGroupedByExecGroups();

    /**
     * Finds all the kpis with the given alias and aggregation period.
     *
     * @param alias             The alias the kpis should match.
     * @param aggregationPeriod The aggregation period the kpis should have
     * @return {@link List} of the names of the above-mentioned kpis
     */
    List<String> findAllKpiNamesWithAliasAndAggregationPeriod(String alias, int aggregationPeriod);

    /**
     * Counts KPIs with the given alias and aggregation period.
     *
     * @param connection {@link Connection} to the database
     * @param table      {@link Table} table object
     * @return an integer, the number of KPIs for the given table
     */
    int countKpisWithAliasAndAggregationPeriod(Connection connection, Table table);

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
     * Updates a definition.
     *
     * @param connection          {@link Connection} to the database.
     * @param kpiDefinitionEntity {@link  KpiDefinitionEntity} to update.
     * @throws SQLException If any error occurs during update of <strong>definition</strong>.
     */
    void update(Connection connection, KpiDefinitionEntity kpiDefinitionEntity, UUID collectionId) throws SQLException;

    /**
     * Sets the time_deleted column to the current timestamp for a list of KpiDefinitions.
     *
     * @param connection         {@link Connection} to the database.
     * @param kpiDefinitionNames {@link List} of {@link String} KpiDefinitionNames to update the time_deleted column for.
     */
    void softDelete(Connection connection, List<String> kpiDefinitionNames, UUID collectionId) throws SQLException;

    /**
     * Saves a definition.
     *
     * @param connection          {@link Connection} to the database.
     * @param kpiDefinitionEntity {@link  KpiDefinitionEntity} to save.
     * @throws SQLException If any error occurs during persistence of <strong>definition</strong>.
     */
    void save(Connection connection, KpiDefinitionEntity kpiDefinitionEntity) throws SQLException;

    /**
     * Saves definitions.
     *
     * @param connection            {@link Connection} to the database.
     * @param kpiDefinitionEntities {@link List} of {@link KpiDefinitionEntity}s to save.
     * @throws SQLException If any error occurs during persistence of <strong>definition</strong>.
     */
    void saveAll(Connection connection, List<KpiDefinitionEntity> kpiDefinitionEntities) throws SQLException;

    /**
     * Finds {@link  KpiDefinitionEntity} by execution group.
     *
     * @param executionGroup {@link ExecutionGroup} The execution group of the kpi definition.
     * @return {@link List} of {@link KpiDefinitionEntity}s
     */
    List<KpiDefinitionEntity> findKpiDefinitionsByExecutionGroup(String executionGroup);

    /**
     * Finds on demand {@link KpiDefinitionEntity} by calculation id.
     *
     * @param calculationId The calculation id of the calculation.
     * @return {@link List} of all the {@link KpiDefinitionEntity}s, calculated with calculation calculationId.
     */
    List<KpiDefinitionEntity> findOnDemandKpiDefinitionsByCalculationId(UUID calculationId);

    /**
     * Saves the relations between on demand kpis and calculation.
     *
     * @param kpiNames      a {@link Set} of the names of the on demand kpis in the calculation
     * @param calculationId the unique IDs of the calculation
     */
    void saveOnDemandCalculationRelation(Set<String> kpiNames, UUID calculationId);

    /**
     * Deletes KPI definitions in a collection
     *
     * @param connection The connection to the database
     * @param kpiNames   {@link Collection} of the KPI names should be deleted
     */
    void deleteKpiDefinitionsByName(Connection connection, Collection<String> kpiNames);

    /**
     * Finds a {@link KpiDefinitionEntity} by KPI name.
     *
     * @param name name of the kpi
     * @return the {@link KpiDefinitionEntity} matching the name.
     * @throws EntityNotFoundException when KPI was not found by the provided name
     */
    default KpiDefinitionEntity forceFindByName(final String name, final UUID collectionId) {
        return findByName(name, collectionId).orElseThrow(() -> new EntityNotFoundException(String.format(
                "KPI was not found by name '%s'", name
        )));
    }
}
