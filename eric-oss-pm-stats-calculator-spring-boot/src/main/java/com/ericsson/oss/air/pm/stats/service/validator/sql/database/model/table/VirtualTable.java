/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;
import static lombok.AccessLevel.PRIVATE;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.api.VirtualTableReference;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = PRIVATE)
public final class VirtualTable implements VirtualTableReference {
    private final Table table;

    public static VirtualTable virtualTable(final Table table) {
        ensureValidTable(table);
        return new VirtualTable(table);
    }

    public static VirtualTable virtualTable(final String table) {
        return virtualTable(table(table));
    }

    @Override
    public int aggregationPeriod() {
        return table.getAggregationPeriod();
    }

    @Override
    public String name() {
        return table.getName();
    }

    @Override
    public String alias() {
        return table.getAlias();
    }

    @Override
    public String toString() {
        return name();
    }

    private static void ensureValidTable(final Table table) {
        //  Can we get the alias from the table?
        table.getAlias();
    }
}
