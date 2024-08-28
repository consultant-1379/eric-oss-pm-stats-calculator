/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.api;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

public interface VirtualTableReference {
    int aggregationPeriod();

    String name();

    default String alias() {
        return name();
    }

    default Table asTable() {
        return table(name());
    }
}
