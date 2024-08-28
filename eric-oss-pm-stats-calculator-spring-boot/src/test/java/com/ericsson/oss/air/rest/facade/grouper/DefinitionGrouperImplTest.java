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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.adapter.KpiDefinitionAdapter;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.repository.util.table.model.TableDefinitions;

import kpi.model.KpiDefinitionRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefinitionGrouperImplTest {
    @Mock KpiDefinitionAdapter kpiDefinitionAdapterMock;
    @InjectMocks DefinitionGrouperImpl objectUnderTest;

    @Test
    void shouldGroupByOutputTable(@Mock final KpiDefinitionRequest kpiDefinitionRequestMock) {
        final KpiDefinitionEntity expectedDefinitionEntity1 = entity("definition1", "alias1", 1_440);
        final KpiDefinitionEntity expectedDefinitionEntity2 = entity("definition2", "alias1", 1_440);
        final KpiDefinitionEntity expectedDefinitionEntity3 = entity("definition3", "alias2", 60);
        final KpiDefinitionEntity expectedDefinitionEntity4 = entity("definition4", "alias2", 60);

        when(kpiDefinitionAdapterMock.toListOfEntities(kpiDefinitionRequestMock)).thenReturn(List.of(expectedDefinitionEntity1, expectedDefinitionEntity2, expectedDefinitionEntity3, expectedDefinitionEntity4));
        final List<TableDefinitions> actual = objectUnderTest.groupByOutputTable(kpiDefinitionRequestMock);

        verify(kpiDefinitionAdapterMock).toListOfEntities(kpiDefinitionRequestMock);
        Assertions.assertThat(actual).containsExactlyInAnyOrder(
                TableDefinitions.of(Table.of("kpi_alias1_1440"), Set.of(expectedDefinitionEntity1, expectedDefinitionEntity2)),
                TableDefinitions.of(Table.of("kpi_alias2_60"), Set.of(expectedDefinitionEntity3, expectedDefinitionEntity4))
        );
    }

    static  KpiDefinitionEntity entity(final String name, final String alias, final int aggregationPeriod) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withName(name);
        builder.withAggregationPeriod(aggregationPeriod);
        builder.withAlias(alias);
        return builder.build();
    }
}