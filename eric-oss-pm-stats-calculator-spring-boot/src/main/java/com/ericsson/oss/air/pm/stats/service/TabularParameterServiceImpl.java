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
import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiPersistenceException;
import com.ericsson.oss.air.pm.stats.model.entity.TabularParameter;
import com.ericsson.oss.air.pm.stats.repository.api.TabularParameterRepository;
import com.ericsson.oss.air.pm.stats.service.api.TabularParameterService;

import kpi.model.KpiDefinitionRequest;
import kpi.model.ondemand.OnDemandTabularParameter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Stateless
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class TabularParameterServiceImpl implements TabularParameterService {

    @Inject
    private TabularParameterRepository tabularParameterRepository;

    @Override
    public void insert(final KpiDefinitionRequest kpiDefinition, final UUID collectionId) {
        final List<OnDemandTabularParameter> tabularParameters = kpiDefinition.onDemand().tabularParameters();
        try (final Connection connection = DriverManager.getConnection(getKpiServiceJdbcConnection(), getKpiServiceJdbcProperties())) {
            for (final OnDemandTabularParameter tabularParameter : tabularParameters) {
                tabularParameterRepository.saveTabularParameter(connection, tabularParameter, collectionId);
            }
        } catch (final SQLException e) {
            throw new KpiPersistenceException("Tabular parameters could not be saved into the table.", e);
        }
    }

    @Override
    public List<TabularParameter> findAllTabularParameters() {
        return tabularParameterRepository.findAllTabularParameters();
    }
}
