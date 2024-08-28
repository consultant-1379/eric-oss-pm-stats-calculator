/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.api;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.model.entity.Parameter;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableCreationInformation;

public interface SqlTableGenerator {
    /**
     * To generate output table SQL statement.
     *
     * @param tableCreationInformation {@link TableCreationInformation} containing necessary information to create table.
     * @param validAggregationElements the {@link Map} of valid aggregation element names and types.
     * @return SQL statement to create output table.
     */
    String generateOutputTable(TableCreationInformation tableCreationInformation, Map<String, KpiDataType> validAggregationElements);

    String generateTabularParameterTableSql(List<Parameter> parameters, String tabularParameterName);
}
