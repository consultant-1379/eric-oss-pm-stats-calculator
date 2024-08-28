/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service;

import static lombok.AccessLevel.PUBLIC;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.repository.api.DimensionTablesRepository;
import com.ericsson.oss.air.pm.stats.service.api.DimensionTablesService;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Stateless

@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class DimensionTablesServiceImpl implements DimensionTablesService {

    @Inject
    private DimensionTablesRepository dimensionTablesRepository;

    public void save(final Connection connection, final Collection<String> tableNames, final UUID calculationId) throws SQLException {
        dimensionTablesRepository.save(connection, tableNames, calculationId);
    }

    @Override
    public List<String> findTableNamesForCalculation(final UUID calculationId) {
        return dimensionTablesRepository.findTableNamesForCalculation(calculationId);
    }

    @Override
    public Set<String> findLostTableNames() {
        return dimensionTablesRepository.findLostTableNames();
    }
}
