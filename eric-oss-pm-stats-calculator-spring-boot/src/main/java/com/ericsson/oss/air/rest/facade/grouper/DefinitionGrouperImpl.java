/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.facade.grouper;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.adapter.KpiDefinitionAdapter;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableDefinitions;
import com.ericsson.oss.air.rest.facade.grouper.api.DefinitionGrouper;

import kpi.model.KpiDefinitionRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefinitionGrouperImpl implements DefinitionGrouper {

    private final  KpiDefinitionAdapter kpiDefinitionAdapter;

    @Override
    public LinkedList<TableDefinitions> groupByOutputTable(@NonNull final KpiDefinitionRequest definitions) {
        final List<KpiDefinitionEntity> kpiDefinitionEntities = kpiDefinitionAdapter.toListOfEntities(definitions);
        return kpiDefinitionEntities.stream().collect(Collectors.collectingAndThen(
                Collectors.groupingBy(
                        DefinitionGrouperImpl::outputTable,
                        Collectors.toCollection(LinkedHashSet<KpiDefinitionEntity>::new)),
                DefinitionGrouperImpl::toTableDefinitions)
        );
    }

    private static LinkedList<TableDefinitions> toTableDefinitions(@NonNull final Map<? extends Table, ? extends LinkedHashSet<KpiDefinitionEntity>> result) {
        return result.entrySet()
                     .stream()
                     .map(entry -> TableDefinitions.of(entry.getKey(), entry.getValue()))
                     .collect(Collectors.toCollection(LinkedList::new));
    }

    private static Table outputTable(final KpiDefinitionEntity kpiDefinitionEntity) {
        return Table.of(kpiDefinitionEntity.tableName());
    }
}
