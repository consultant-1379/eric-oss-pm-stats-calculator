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
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ejb.Local;

import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;

import kpi.model.KpiDefinitionRequest;

@Local
public interface RetentionPeriodService {
    void insertRetentionPeriod(KpiDefinitionRequest kpiDefinitionRequest, UUID collectionId);

    void deleteFromTableLevelRetention(Connection connection, String tableName) throws SQLException;

    List<RetentionPeriodTableEntity> findTablePeriods(UUID collectionId);

    Optional<RetentionPeriodCollectionEntity> findCollectionPeriods(UUID collectionId);
}
