/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.facade.grouper.api;

import java.util.LinkedList;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableDefinitions;

import kpi.model.KpiDefinitionRequest;

public interface DefinitionGrouper {
    /**
     * Groups {@link kpi.model.api.table.definition.KpiDefinition}s in a {@link KpiDefinitionRequest} by the output {@link Table} they are stored in the database.
     *
     * @param definitions
     *         {@link KpiDefinitionRequest} to group by
     * @return {@link LinkedList} of {@link TableDefinitions} containing definitions attached to an output {@link Table}
     */
    LinkedList<TableDefinitions> groupByOutputTable(KpiDefinitionRequest definitions);
}
