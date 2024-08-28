/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.model;

import java.util.Collections;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data(staticConstructor = "of")
public final class TableDefinitions {
    private final Table table;
    private final Set<KpiDefinitionEntity> definitions;

    @SuppressWarnings("unused")
    private TableDefinitions(@NonNull final Table table, @NonNull final Set<KpiDefinitionEntity> definitions) {
        Preconditions.checkArgument(!definitions.isEmpty(), "definitions should not be empty");

        this.table = table;
        this.definitions = Collections.unmodifiableSet(definitions);
    }

    public String getAggregationPeriod() {
        final KpiDefinitionEntity definition = Iterables.get(definitions, 0);
        return String.valueOf(definition.aggregationPeriod());
    }

}
