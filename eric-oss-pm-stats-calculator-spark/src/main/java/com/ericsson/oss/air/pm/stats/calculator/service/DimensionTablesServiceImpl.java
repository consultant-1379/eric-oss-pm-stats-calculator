/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service;

import java.util.List;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.calculator.repository.internal.DimensionTablesRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.DimensionTablesService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DimensionTablesServiceImpl implements DimensionTablesService {

    private final DimensionTablesRepository dimensionRepository;

    @Override
    public List<String> getTabularParameterTableNamesByCalculationId(final UUID calculationId) {
        return dimensionRepository.findTableNamesByCalculationId(calculationId);
    }
}
