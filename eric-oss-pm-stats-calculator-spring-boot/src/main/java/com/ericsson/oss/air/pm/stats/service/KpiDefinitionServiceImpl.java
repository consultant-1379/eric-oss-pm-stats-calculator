/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcConnection;
import static com.ericsson.oss.air.pm.stats.common.env.DatabaseProperties.getKpiServiceJdbcProperties;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_COLLECTION_ID;
import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.adapter.KpiDefinitionAdapter;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiCounter;
import com.ericsson.oss.air.pm.stats.common.metrics.ApiMetricRegistry;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.SimpleKpiDependencyCache;
import com.ericsson.oss.air.pm.stats.repository.api.KpiDefinitionRepository;
import com.ericsson.oss.air.pm.stats.repository.util.transaction.TransactionExecutor;
import com.ericsson.oss.air.pm.stats.service.api.DatabaseService;
import com.ericsson.oss.air.pm.stats.service.api.KpiDefinitionService;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;

import kpi.model.KpiDefinitionRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class KpiDefinitionServiceImpl implements KpiDefinitionService {

    @Inject
    private KpiDefinitionRepository kpiDefinitionRepository;
    @Inject
    private SimpleKpiDependencyCache simpleKpiDependencyCache;
    @Inject
    private KpiDefinitionAdapter kpiDefinitionAdapter;
    @Inject
    private DatabaseService databaseService;
    @Inject
    private ApiMetricRegistry apiMetricRegistry;

    @Override
    public void upsert(final KpiDefinitionRequest definitions) {
        final List<KpiDefinitionEntity> entities = kpiDefinitionAdapter.toListOfEntities(definitions);
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            for (final KpiDefinitionEntity entity : entities) {
                upsertDefinition(connection, entity);
            }
        } catch (final SQLException e) {
            throw new KpiPersistenceException("Error persisting into 'kpi_definition' table.", e);
        }
        simpleKpiDependencyCache.populateCache();
    }

    @Override
    public void insert(final KpiDefinitionRequest definitions, final UUID collectionId) throws KpiPersistenceException {
        final List<KpiDefinitionEntity> entities = kpiDefinitionAdapter.toListOfCollectionEntities(definitions, collectionId);

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            kpiDefinitionRepository.saveAll(connection, entities);
            apiMetricRegistry.counter(ApiCounter.DEFINITION_PERSISTED_KPI).inc(entities.size());
        } catch (final SQLException e) {
            throw new KpiPersistenceException("Error persisting into 'kpi_definition' table.", e);
        }
        simpleKpiDependencyCache.populateCache();
    }

    @Override
    public void updateWithoutColumnTypeChange(final KpiDefinitionEntity kpiDefinitionEntity, final UUID collectionId) {
        update(kpiDefinitionEntity,collectionId, connection -> { });
    }

    @Override
    public void updateWithColumnTypeChange(final KpiDefinitionEntity kpiDefinitionEntity, final UUID collectionId) {
        update(kpiDefinitionEntity, collectionId,  connection -> databaseService.changeColumnType(connection, kpiDefinitionEntity));
    }

    @Override
    public void softDelete(final List<String> kpiDefinitionNames, final UUID collectionId) {
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            kpiDefinitionRepository.softDelete(connection, kpiDefinitionNames, collectionId);
        } catch (final SQLException e) {
            throw new KpiPersistenceException("Error updating time_deleted field for kpi definitions.", e);
        }
        simpleKpiDependencyCache.populateCache();
    }

    @Override
    public List<KpiDefinitionEntity> findAll() {
        return kpiDefinitionRepository.findAll();
    }

    @Override
    public List<KpiDefinitionEntity> findAll(final UUID collectionId) {
        return kpiDefinitionRepository.findAll(collectionId);
    }


    @Override
    public List<KpiDefinitionEntity> findAllIncludingSoftDeleted() {
        return kpiDefinitionRepository.findAllIncludingSoftDeleted();
    }

    @Override
    public List<KpiDefinitionEntity> findAllIncludingSoftDeleted(final UUID collectionId){
        return kpiDefinitionRepository.findAllIncludingSoftDeleted(collectionId);
    }



    @Override
    public List<KpiDefinitionEntity> findComplexKpis() {
        return kpiDefinitionRepository.findComplexKpis();
    }

    @Override
    public Set<String> findAllSimpleExecutionGroups() {
        return kpiDefinitionRepository.findAllSimpleExecutionGroups();
    }

    @Override
    public Set<String> findAllComplexExecutionGroups() {
        return kpiDefinitionRepository.findAllComplexExecutionGroups();
    }

    @Override
    public Map<String, List<String>> findAllSimpleKpiNamesGroupedByExecGroups() {
        return kpiDefinitionRepository.findAllSimpleKpiNamesGroupedByExecGroups();
    }

    @Override
    public Set<String> findAllKpiNames() {
        return kpiDefinitionRepository.findAllKpiNames();
    }

    @Override
    public Set<String> findAllKpiNames(final UUID collectionId) {
        return kpiDefinitionRepository.findAllKpiNames(collectionId);
    }

    @Override
    public Map<String, List<String>> findAllComplexKpiNamesGroupedByExecGroups() {
        return kpiDefinitionRepository.findAllComplexKpiNamesGroupedByExecGroups();
    }

    @Override
    public Set<Pair<String, Integer>> findOnDemandAliasAndAggregationPeriods() {
        return kpiDefinitionRepository.findOnDemandAliasAndAggregationPeriods();
    }

    @Override
    public Set<Pair<String, Integer>> findScheduledAliasAndAggregationPeriods() {
        return kpiDefinitionRepository.findScheduledAliasAndAggregationPeriods();
    }

    @Override
    public List<String> findAllKpiNamesFromSameTableAsKpiDefinition(final KpiDefinitionEntity kpiDefinition) {
        return kpiDefinitionRepository.findAllKpiNamesWithAliasAndAggregationPeriod(kpiDefinition.alias(), kpiDefinition.aggregationPeriod());
    }

    @Override
    public boolean doesTableHaveAnyKpisLeft(final Connection connection, final Table table) {
        return kpiDefinitionRepository.countKpisWithAliasAndAggregationPeriod(connection, table) > 0;
    }

    @Override
    public List<KpiDefinitionEntity> findKpiDefinitionsByExecutionGroup(final String executionGroup) {
        return kpiDefinitionRepository.findKpiDefinitionsByExecutionGroup(executionGroup);
    }

    @Override
    public <O> Collection<O> findKpiDefinitionsByExecutionGroup(final String executionGroup,
                                                                final Function<? super KpiDefinitionEntity, ? extends O> mapper) {
        return CollectionHelpers.transform(findKpiDefinitionsByExecutionGroup(executionGroup), mapper);
    }

    @Override
    public Collection<String> findKpiDefinitionNamesByExecutionGroup(final String executionGroup) {
        return findKpiDefinitionsByExecutionGroup(executionGroup, KpiDefinitionEntity::name);
    }

    @Override
    public List<KpiDefinitionEntity> findOnDemandKpiDefinitionsByCalculationId(final UUID calculationId) {
        return kpiDefinitionRepository.findOnDemandKpiDefinitionsByCalculationId(calculationId);
    }

    @Override
    public KpiDefinitionEntity forceFindByName(final String name, final UUID collectionId) {
        return kpiDefinitionRepository.forceFindByName(name, collectionId);
    }

    @Override
    public List<KpiDefinitionEntity> findByNames(final Set<String> names) {
        return kpiDefinitionRepository.findByNames(names);
    }

    @Override
    public boolean doesContainData() {
        return kpiDefinitionRepository.count() != 0;
    }

    @Override
    public void saveOnDemandCalculationRelation(final Set<String> kpiNames, final UUID calculationId) {
        kpiDefinitionRepository.saveOnDemandCalculationRelation(kpiNames, calculationId);
    }

    @Override
    public long countComplexKpi() {
        return kpiDefinitionRepository.countComplexKpi();
    }

    @Override
    public void deleteKpiDefinitionsByName(final Connection connection, final Collection<String> kpiNames) {
        kpiDefinitionRepository.deleteKpiDefinitionsByName(connection, kpiNames);
    }

    /**
     * Upsert implemented with two separate calls since <strong>H2</strong> does not support <strong>PostgreSQL</strong>'s
     * <pre>{@code
     *  INSERT ... ON CONFLICT (...) DO UPDATE
     * }</pre>
     * mechanism, thus cannot be tested.
     * <br>
     * <strong>TODO:</strong> Once testing is available, migrate to use <strong>PostgreSQL</strong>'s
     * built-in mechanism to implement upsert.
     */
    private void upsertDefinition(final Connection connection, final KpiDefinitionEntity kpiDefinitionEntity) throws
            SQLException {
        try {
            kpiDefinitionRepository.save(connection, kpiDefinitionEntity);
        } catch (final SQLException e) {
            log.warn("Error persisting KPI Definition with name '{}'", kpiDefinitionEntity.name(), e);
            kpiDefinitionRepository.update(connection, kpiDefinitionEntity, DEFAULT_COLLECTION_ID);
        }
    }

    private void update(final KpiDefinitionEntity kpiDefinitionEntity,
                        final UUID collectionId,
                        final Consumer<Connection> afterSuccessfullUpdateConsumer) {
        try {
            TransactionExecutor.executeInTransaction(connection -> {
                kpiDefinitionRepository.update(connection, kpiDefinitionEntity, collectionId);
                afterSuccessfullUpdateConsumer.accept(connection);
            });
        } catch (final SQLException e) {
            throw new KpiPersistenceException("Error persisting into 'kpi_definition' table.", e);
        }
        simpleKpiDependencyCache.populateCache();
    }
}