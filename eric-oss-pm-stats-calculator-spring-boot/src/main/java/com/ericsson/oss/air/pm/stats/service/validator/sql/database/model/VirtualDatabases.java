/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database.model;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.Indents.indent;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualAlias.virtualAlias;
import static com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualTable.virtualTable;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.api.VirtualTableReference;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@EqualsAndHashCode
@Accessors(fluent = true)
@RequiredArgsConstructor(access = PRIVATE)
public final class VirtualDatabases {
    private final Map<Datasource, VirtualDatabase> datasourceVirtualDatabase = new HashMap<>();

    public static VirtualDatabases empty() {
        return new VirtualDatabases();
    }

    public Optional<Table> locateTable(@NonNull final Datasource datasource, final Column column) {
        final VirtualDatabase virtualDatabase = datasourceVirtualDatabase.get(datasource);
        return virtualDatabase == null ? Optional.empty() : virtualDatabase.locateTable(column);
    }

    public void registerDatabase(final VirtualDatabase virtualDatabase) {
        final Datasource datasource = virtualDatabase.datasource();
        if (nonNull(datasourceVirtualDatabase.put(datasource, virtualDatabase))) {
            throw new IllegalArgumentException(String.format("'%s' '%s' already registered", Datasource.class.getSimpleName(), datasource.getName()));
        }
    }

    public boolean doesContain(final Datasource datasource, @Nullable final Table table, final Integer aggregationPeriod, final Column column) {
        final VirtualDatabase virtualDatabase = datasourceVirtualDatabase.get(datasource);
        if (virtualDatabase == null) {
            return false;
        }

        if (table == null) {
            return virtualDatabase.doesContain(column);
        }

        final VirtualTableReference virtualTableReference = virtualTableReference(datasource, table, aggregationPeriod);
        return virtualDatabase.doesContain(virtualTableReference, column);
    }

    private static VirtualTableReference virtualTableReference(final Datasource datasource, final Table table, final Integer aggregationPeriod) {
        return doesRequireAlias(datasource) ? virtualAlias(table.getName(), aggregationPeriod) : virtualTable(table);
    }

    private static boolean doesRequireAlias(@NonNull final Datasource datasource) {
        return datasource.isInMemory() || TABULAR_PARAMETERS.equals(datasource);
    }

    @Override
    public String toString() {
        final Stream<String> databases = datasourceVirtualDatabase.values().stream().map(database -> indent(database.toString(), 2, 1));
        return getClass().getSimpleName() + ' ' + join(
                lineSeparator(), "[", join(lineSeparator(), databases.collect(toList())), "]"
        );
    }

}
