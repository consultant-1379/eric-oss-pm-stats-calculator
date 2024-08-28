/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.api;

import java.util.List;
import java.util.UUID;

public interface DimensionTablesService {
    /**
     * Returns dimension table names for a calculation.
     *
     * @param calculationId
     *      {@link UUID} the calculation for the data needed.
     * @return the table names of the tabular parameter.
     */
    List<String> getTabularParameterTableNamesByCalculationId(UUID calculationId);
}
