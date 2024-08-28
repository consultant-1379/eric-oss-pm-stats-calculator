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

import static com.ericsson.oss.air.pm.stats.common.model.collection.Column.AGGREGATION_BEGIN_TIME;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Column.AGGREGATION_END_TIME;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SourceColumn;
import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode
@NoArgsConstructor(staticName = "of")
public class TableColumns {
    @Delegate(types = MapDelegate.class)
    private final Map<Table, Set<Column>> map = new HashMap<>();

    public boolean computeIfAbsent(@NonNull final SourceColumn sourceColumn) {
        return computeIfAbsent(sourceColumn.getTable()).add(Column.of(sourceColumn.getColumn()));
    }

    public Set<Column> computeIfAbsent(final Table key) {
        return computeIfAbsent(key, newTables -> new HashSet<>());
    }

    public Set<Column> dimColumns(final Table table) {
        return safeRead(table);
    }

    public Set<Column> factColumns(final Table table) {
        final Set<Column> columns = safeRead(table);

        columns.add(AGGREGATION_BEGIN_TIME);
        columns.add(AGGREGATION_END_TIME);

        return columns;
    }

    private Set<Column> safeRead(final Table table) {
        return Objects.requireNonNull(
                map.get(table),
                () -> String.format("Table '%s' is not found in the '%s'", table.getName(), getClass().getSimpleName())
        );
    }

    public static TableColumns merge(@NonNull final TableColumns left, @NonNull final TableColumns right) {
        final TableColumns result = of();

        left.forEach(((table, columns) -> columns.forEach(column -> result.computeIfAbsent(table).add(column))));
        right.forEach(((table, columns) -> columns.forEach(column -> result.computeIfAbsent(table).add(column))));

        return result;
    }

    public interface MapDelegate {
        int size();

        Set<Column> get(Table key);

        Set<Column> put(Table key, Set<Column> value);

        void forEach(BiConsumer<Table, Set<Column>> action);

        Set<Entry<Table, Set<Column>>> entrySet();

        Set<Column> computeIfAbsent(Table key, Function<? super Table, ? extends Set<Column>> mappingFunction);
    }
}
