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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@EqualsAndHashCode
@Accessors(fluent = true)
@RequiredArgsConstructor(access = PRIVATE)
public final class Reference {
    @Nullable private final Datasource datasource;
    @Nullable private final Table table;
    @Nullable private final Column column;
    @Nullable private final Alias alias;
    @Nullable private final String parsedFunctionSql;

    @Exclude private final Set<Location> locations = EnumSet.noneOf(Location.class);

    public static Reference of(final Datasource datasource, final Table table, final Column column, final Alias alias) {
        final Reference reference = new Reference(datasource, table, column, alias, null);

        if (nonNull(datasource) && isNull(table)) {
            throw new IllegalArgumentException(String.format(
                        "When '%s' present then '%s' must be present as well '%s'", Datasource.class.getSimpleName(), Table.class.getSimpleName(), reference
            ));
        }

        if (nonNull(table) && isNull(column)) {
            throw new IllegalArgumentException(String.format(
                        "When '%s' present then '%s' must be present as well '%s'", Table.class.getSimpleName(), Column.class.getSimpleName(), reference
            ));
        }

        return reference;
    }

    public static Reference of(final Datasource datasource, final Table table, final Column column, final Alias alias, final String parsedFunctionSql) {
        final Reference reference = new Reference(datasource, table, column, alias, parsedFunctionSql);

        if (nonNull(datasource) && isNull(table)) {
            throw new IllegalArgumentException(String.format(
                    "When '%s' present then '%s' must be present as well '%s'", Datasource.class.getSimpleName(), Table.class.getSimpleName(), reference
            ));
        }

        if (nonNull(table) && isNull(column)) {
            throw new IllegalArgumentException(String.format(
                    "When '%s' present then '%s' must be present as well '%s'", Table.class.getSimpleName(), Column.class.getSimpleName(), reference
            ));
        }

        if (isNull(parsedFunctionSql)) {
            throw new IllegalArgumentException(
                    "When referencing a function, it must not be null."
            );
        }

        return reference;
    }

    public void addLocation(final Location location) {
        locations.add(location);
    }

    public void addLocation(final Collection<Location> locations) {
        this.locations.addAll(locations);
    }

    public Optional<Datasource> datasource() {
        return ofNullable(datasource);
    }

    public Optional<Table> table() {
        return ofNullable(table);
    }

    public Optional<Column> column() {
        return ofNullable(column);
    }

    public Optional<Alias> alias() {
        return ofNullable(alias);
    }

    public boolean hasDatasource() {
        return datasource != null;
    }

    public boolean hasTable() {
        return table != null;
    }

    public boolean hasColumn() {
        return column != null;
    }

    public boolean hasAlias() {
        return alias != null;
    }

    public boolean hasParsedFunctionSql() {
        return parsedFunctionSql != null;
    }

    /**
     * Returns true if the {@link Reference} is encapsulating parameterized aggregation element with format like <strong>'${param.execution_id}' AS
     * execution_id</strong>
     * <br>
     * <strong>NOTE</strong>: The applied logic can also detect if the parameter in the aggregation element is resolved.
     * <br>
     * <table style="border: 1px solid">
     *     <caption>Visualized parameterized aggregation elements</caption>
     *     <thead>
     *         <tr>
     *             <th style=>datasource</th>
     *             <th style=>table</th>
     *             <th style=>column</th>
     *             <th style=>alias</th>
     *             <th style=>isParameterized</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td></td>
     *             <td></td>
     *             <td></td>
     *             <td style="text-align: center">X</td>
     *             <td>true (parameter unresolved)</td>
     *         </tr>
     *         <tr>
     *             <td></td>
     *             <td></td>
     *             <td style="text-align: center">X</td>
     *             <td style="text-align: center">X</td>
     *             <td>true (parameter resolved)</td>
     *          </tr>
     *     </tbody>
     * </table>
     *
     * @return true if the {@link Reference} is parameterized aggregation element otherwise false
     */
    public boolean isParameterizedAggregationElement() {
        final boolean hasNoDatasourceTable = !hasDatasource() && !hasTable();
        final boolean unresolvedParameterizedElement = hasNoDatasourceTable && !hasColumn() && hasAlias();
        final boolean resolvedParameterizedElement = hasNoDatasourceTable && hasColumn() && hasAlias();
        return !isFunctionAggregationElement() && (unresolvedParameterizedElement || resolvedParameterizedElement);
    }

    /**
     * Returns true if the {@link Reference} is encapsulating a UDF aggregation element with format like
     * <strong>FDN_PARSE([table].[column], arg) AS agg_element</strong>.
     *
     * @return true if the {@link Reference} is a UDF aggregation element, otherwise false
     */
    public boolean isFunctionAggregationElement() {
        return hasParsedFunctionSql() && hasTable() && hasColumn() && hasAlias();
    }

    public Column requiredColumn() {
        if (column == null) {
            throw new IllegalArgumentException("'column' is not present");
        }

        return column;
    }

    public Table requiredTable() {
        if (table == null) {
            throw new IllegalArgumentException("'table' is not present");
        }

        return table;
    }

    public String aliasOrColumnName() {
        final Optional<String> aliasName = alias().map(Alias::name);
        return aliasName.or(() -> column().map(Column::getName)).orElseThrow(
                () -> new IllegalArgumentException("alias and column are null")
        );
    }

    public String columnOrAliasName() {
        final Optional<String> columnName = column().map(Column::getName);
        return columnName.or(() -> alias().map(Alias::name)).orElseThrow(
                () -> new IllegalArgumentException("alias and column are null")
        );
    }

    public String tableName() {
        final Optional<String> tableName = table().map(Table::getName);
        return tableName.orElseThrow(() -> new IllegalArgumentException("table name is null"));
    }

    public String toString(final boolean appendLocations) {
        return doToString(appendLocations);
    }

    @Override
    public String toString() {
        return doToString(true);
    }

    private String doToString(final boolean appendLocations) {
        final List<String> parts = new ArrayList<>();

        datasource().ifPresent(source -> parts.add(source.getName()));
        table().ifPresent(source -> parts.add(source.getName()));
        column().ifPresent(source -> parts.add(source.getName()));

        final StringBuilder stringBuilder = new StringBuilder(String.join(".", parts));

        alias().ifPresent(source -> {
            if (column().isEmpty()) {
                stringBuilder.append("<parameter>");
            }
            stringBuilder.append(" AS ").append(alias.name());
        });

        if (appendLocations) {
            stringBuilder.append(locations.stream().sorted().collect(toList()));
        }

        return stringBuilder.toString();
    }

    public enum Location {
        AGGREGATION_ELEMENTS,
        EXPRESSION,
        FILTERS;

    }

}
