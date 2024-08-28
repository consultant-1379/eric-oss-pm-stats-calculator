/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import kpi.model.api.table.Table;
import kpi.model.api.table.definition.KpiDefinition;
import kpi.model.api.table.definition.KpiDefinitions;

public interface KpiDefinitionTable<T extends Table> {
    List<T> kpiOutputTables();

    default Set<KpiDefinition> definitions() {
        final List<T> outputTables = kpiOutputTables();
        return outputTables.stream()
                .map(Table::kpiDefinitions)
                .map(KpiDefinitions::value)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    default boolean isEmpty() {
        return kpiOutputTables().isEmpty();
    }
}
