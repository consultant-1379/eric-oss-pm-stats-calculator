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
import static com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils.collectCollectionLevelEntity;
import static com.ericsson.oss.air.pm.stats.service.util.retention.RetentionPeriodUtils.collectTableLevelEntities;
import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.repository.api.CollectionRetentionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.TableRetentionRepository;
import com.ericsson.oss.air.pm.stats.service.api.RetentionPeriodService;

import kpi.model.KpiDefinitionRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;


@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class RetentionPeriodServiceImpl implements RetentionPeriodService {
    @Inject
    private TableRetentionRepository tableRetentionRepository;
    @Inject
    private CollectionRetentionRepository collectionRetentionRepository;

    @Override
    public void insertRetentionPeriod(final KpiDefinitionRequest kpiDefinitionRequest, final UUID collectionId) {
        final List<RetentionPeriodTableEntity> retentionPeriodTableLevelEntities = collectTableLevelEntities(kpiDefinitionRequest, collectionId);
        final Optional<RetentionPeriodCollectionEntity> retentionPeriodCollectionLevelEntity = collectCollectionLevelEntity(kpiDefinitionRequest, collectionId);

        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            if (CollectionUtils.isNotEmpty(retentionPeriodTableLevelEntities)) {
                tableRetentionRepository.saveAll(connection, retentionPeriodTableLevelEntities);
            }

            if (retentionPeriodCollectionLevelEntity.isPresent()) {
                collectionRetentionRepository.save(connection, retentionPeriodCollectionLevelEntity.get());
            }
        } catch (final SQLException e) {
            throw new KpiPersistenceException("Error persisting into 'retention_configuration' tables.", e);
        }
    }

    @Override
    public void deleteFromTableLevelRetention(final Connection connection, final String tableName) throws SQLException {
        tableRetentionRepository.deleteRetentionForTables(connection, tableName);
    }

    @Override
    public List<RetentionPeriodTableEntity> findTablePeriods(final UUID collectionId) {
        return tableRetentionRepository.findByCollectionId(collectionId);
    }

    @Override
    public Optional<RetentionPeriodCollectionEntity> findCollectionPeriods(final UUID collectionId) {
        return collectionRetentionRepository.findByCollectionId(collectionId);
    }
}
