/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database.model;

import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.Indents.indent;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.VirtualAlias;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.api.VirtualTableReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

@EqualsAndHashCode
@Accessors(fluent = true)
@RequiredArgsConstructor(access = PRIVATE)
public final class VirtualDatabase {
    @Getter
    private final Datasource datasource;

    private final MultiValuedMap<VirtualTableReference, Column> tables = new HashSetValuedHashMap<>();


    public static VirtualDatabase empty(@NonNull final Datasource datasource) {
        return new VirtualDatabase(datasource);
    }

    public static VirtualDatabase toInMemory(@NonNull final Datasource datasource, @NonNull final VirtualDatabase virtualDatabase) {
        ensureInMemory(datasource);
        ensureNonInMemory(virtualDatabase.datasource());

        final VirtualDatabase result = empty(datasource);
        virtualDatabase.tables.asMap().forEach((virtualTableReference, columns) -> {
            for (final Column column : columns) {
                result.addColumn(VirtualAlias.from(virtualTableReference), column);
            }
        });

        return result;
    }

    public void addColumn(@NonNull final VirtualTableReference virtualTableReference, final Column column) {
        tables.put(virtualTableReference, column);
    }

    public Optional<Table> locateTable(@NonNull final Column column) {
        final List<Table> result = new ArrayList<>();

        tables.asMap().forEach((virtualTableReference, columns) -> {
            if (columns.contains(column)) {
                result.add(virtualTableReference.asTable());
            }
        });

        if (result.isEmpty()) {
            return Optional.empty();
        }

        if (result.size() == 1) {
            return Optional.of(result.get(0));
        }

        //  Ensure only one table can be found
        throw new IllegalStateException(String.format(
                "The required '%s' appears in multiple tables '%s'",
                column.getName(), result.stream().map(Table::getName).collect(toList())
        ));
    }

    public boolean doesContain(final VirtualTableReference virtualTableReference) {
        return tables.containsKey(virtualTableReference);
    }

    public boolean doesContain(final Column column) {
        return doesContain(null, column);
    }

    public boolean doesContain(@Nullable final VirtualTableReference virtualTableReference, final Column column) {
        if (virtualTableReference == null) {
            return tables.values().contains(column);
        }
        return tables.get(virtualTableReference).contains(column);
    }

    private static void ensureInMemory(@NonNull final Datasource datasource) {
        checkArgument(datasource.isInMemory(), String.format(
                "The provided '%s' '%s' '%s' is not in-memory",
                VirtualDatabase.class.getSimpleName(), Datasource.class.getSimpleName(), datasource.getName()
        ));
    }

    private static void ensureNonInMemory(@NonNull final Datasource datasource) {
        checkArgument(datasource.isNonInMemory(), String.format(
                "The provided '%s' '%s' '%s' is in-memory",
                VirtualDatabase.class.getSimpleName(), Datasource.class.getSimpleName(), datasource.getName()
        ));
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder(Datasource.class.getSimpleName()).append(' ');
        stringBuilder.append(datasource.getName());
        stringBuilder.append(" [").append(datasource.isInMemory() ? "ALIAS" : "TABLE").append("] ");

        stringBuilder.append("tables: ");

        if (tables.isEmpty()) {
            stringBuilder.append("[]");
        } else {
            final String lineSeparator = System.lineSeparator();
            stringBuilder.append('[').append(lineSeparator);

            tables.asMap().forEach((virtualTableReference, columns) -> {
                stringBuilder.append(indent(2, 1));
                stringBuilder.append(virtualTableReference.toString());
                stringBuilder.append(": ");
                stringBuilder.append(printColumns(columns));
                stringBuilder.append(lineSeparator);
            });

            stringBuilder.append(']');
        }

        return stringBuilder.toString();
    }

    private static List<String> printColumns(@NonNull final Collection<Column> columns) {
        return columns.stream().map(Column::getName).sorted().collect(toList());
    }
}
