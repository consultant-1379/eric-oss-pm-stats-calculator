/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.domain;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public final class Relation {
    @Nullable private final Datasource datasource;
    @NonNull  private final Table table;
    @Nullable private final Alias alias;

    public static Relation of(final Datasource datasource, final Table table, final Alias alias) {
        return new Relation(datasource, table, alias);
    }

    public String tableName() {
        return table.getName();
    }

    public Optional<Datasource> datasource() {
        return ofNullable(datasource);
    }

    public Optional<Alias> alias() {
        return ofNullable(alias);
    }

    public boolean hasDatasource(final Datasource other) {
        return datasource().map(value -> value.equals(other)).orElse(false);
    }

    public boolean hasTable(final Table other) {
        return table.equals(other);
    }

    public boolean hasAlias(final Alias other) {
        return alias().map(value -> value.equals(other)).orElse(false);
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        datasource().ifPresent(source -> stringBuilder.append(source.getName()).append('.'));
        stringBuilder.append(table.getName());

        alias().ifPresent(value -> stringBuilder.append(" AS ").append(value.name()));

        return stringBuilder.toString();
    }
}
