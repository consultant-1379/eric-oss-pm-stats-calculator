/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceTable;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode
@NoArgsConstructor(staticName = "newInstance")
public class DatasourceTables {
    @Delegate(types = MapDelegate.class)
    private Map<Datasource, Set<Table>> map = new HashMap<>();

    private DatasourceTables(final int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    public static DatasourceTables newInstance(final int initialCapacity) {
        return new DatasourceTables(initialCapacity);
    }

    public boolean containsTable(final Datasource datasource, final Table table) {
        final Set<Table> tables = map.get(datasource);
        if (Objects.isNull(tables)) {
            return false;
        }
        return Objects.nonNull(table) && tables.contains(table);
    }

    public DatasourceTables filterAvailableDataSources(final Collection<Datasource> nonAvailableDataSources) {
        final DatasourceTables datasourceTables = newInstance();

        map.forEach((datasource, tables) -> {
            if (!nonAvailableDataSources.contains(datasource)) {
                datasourceTables.computeIfAbsent(datasource).addAll(tables);
            }
        });

        return datasourceTables;
    }

    public boolean computeIfAbsent(@NonNull final SourceTable sourceTable) {
        return computeIfAbsent(sourceTable.getDatasource()).add(sourceTable.getTable());
    }

    public Set<Table> computeIfAbsent(final Datasource datasource) {
        return computeIfAbsent(datasource, newTables -> new HashSet<>());
    }

    public interface MapDelegate {
        int size();

        Set<Table> get(Datasource key);

        Set<Table> put(Datasource key, Set<Table> value);

        Set<Datasource> keySet();

        void forEach(BiConsumer<Datasource, Set<Table>> action);

        Set<Entry<Datasource, Set<Table>>> entrySet();

        Set<Table> computeIfAbsent(Datasource key, Function<? super Datasource, ? extends Set<Table>> mappingFunction);
    }
}
