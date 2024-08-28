/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.sql.database.resolver;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.TABULAR_PARAMETERS;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static com.ericsson.oss.air.pm.stats.common.sqlparser.util.References.table;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Relation;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.VirtualDatabases;

import lombok.Data;
import lombok.NonNull;

@Data
public final class DatabaseResolver {
    /**
     * KPI Definition aggregation period that is being resolved by this resolver.
     * If the aggregation period is 60 it means that the algorithm in case of in-memory datasources
     * will try to resolve columns from the alias table marked with the represented aggregation period.
     * <p>
     * If the aggregation period is 60 and table is cell_guid then the in memory target table is cell_guid(60)
     */
    private final int virtualAliasAggregationPeriod;
    private final VirtualDatabases virtualDatabases;

    public boolean canResolve(@NonNull final Datasource datasource, final Table table, final Column column) {
        final int actualAggregationPeriod = TABULAR_PARAMETERS.equals(datasource)
                ? DEFAULT_AGGREGATION_PERIOD_INT
                : virtualAliasAggregationPeriod;

        return virtualDatabases.doesContain(datasource, table, actualAggregationPeriod, column);
    }

    public Optional<Relation> anyResolve(@NonNull final Collection<Relation> relations, @Nullable final Table table, final Column column) {
        for (final Relation relation : relations) {
            final Datasource relationDatasource = relation.datasource().orElse(KPI_DB);
            final Table relationTable = relation.alias().map(alias -> table(alias.name())).orElseGet(relation::table);
            if (relationTable.equals(table) && canResolve(relationDatasource, relation.table(), column)) {
                return Optional.of(relation);
            }
        }

        return Optional.empty();
    }
}
