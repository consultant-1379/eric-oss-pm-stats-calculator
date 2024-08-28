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

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static lombok.AccessLevel.PRIVATE;

import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.Alias;
import com.ericsson.oss.air.pm.stats.common.sqlparser.util.References;
import com.ericsson.oss.air.pm.stats.service.validator.sql.database.model.table.api.VirtualTableReference;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = PRIVATE)
public final class VirtualAlias implements VirtualTableReference {
    private final Alias alias;
    private final int aggregationPeriod;

    public static VirtualAlias from(@NonNull final VirtualTableReference virtualTableReference) {
        return virtualAlias(virtualTableReference.alias(), virtualTableReference.aggregationPeriod());
    }

    public static VirtualAlias virtualAlias(final String alias) {
        return virtualAlias(alias, DEFAULT_AGGREGATION_PERIOD_INT);
    }

    public static VirtualAlias virtualAlias(final String alias, final int aggregationPeriod) {
        return new VirtualAlias(References.alias(alias), aggregationPeriod);
    }

    @Override
    public int aggregationPeriod() {
        return aggregationPeriod;
    }

    @Override
    public String name() {
        return alias.name();
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", alias.name(), DEFAULT_AGGREGATION_PERIOD_INT == aggregationPeriod ? "DEFAULT" : aggregationPeriod);
    }
}
