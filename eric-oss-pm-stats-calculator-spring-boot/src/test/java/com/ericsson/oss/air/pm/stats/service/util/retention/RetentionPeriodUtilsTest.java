/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util.retention;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodCollectionEntity.RetentionPeriodCollectionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity;
import com.ericsson.oss.air.pm.stats.model.entity.RetentionPeriodTableEntity.RetentionPeriodTableEntityBuilder;

import kpi.model.KpiDefinitionRequest;
import kpi.model.RetentionPeriod;
import kpi.model.complex.ComplexTable;
import kpi.model.complex.table.definition.optional.ComplexTableRetentionPeriod;
import kpi.model.ondemand.OnDemandTable;
import kpi.model.ondemand.table.optional.OnDemandTableRetentionPeriod;
import kpi.model.simple.SimpleTable;
import kpi.model.simple.table.optional.SimpleTableRetentionPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetentionPeriodUtilsTest {

    @Mock
    SimpleTable simpleTableMock;
    @Mock
    ComplexTable complexTableMock;
    @Mock
    OnDemandTable onDemandTableMock;
    @Mock
    KpiDefinitionRequest kpiDefinitionRequestMock;

    final UUID collectionId = UUID.fromString("89d3713a-5499-4403-8072-1b18235946e9");


    @Test
    void collectTablesWithRetentionPeriodTest() {
        when(simpleTableMock.tableName()).thenReturn("kpi_alias_simple_60");
        when(simpleTableMock.retentionPeriod()).thenReturn(SimpleTableRetentionPeriod.of(5));
        when(complexTableMock.tableName()).thenReturn("kpi_alias_complex_60");
        when(complexTableMock.retentionPeriod()).thenReturn(ComplexTableRetentionPeriod.of(4));
        when(onDemandTableMock.retentionPeriod()).thenReturn(OnDemandTableRetentionPeriod.of(null));

        when(kpiDefinitionRequestMock.tables()).thenReturn(
                List.of(simpleTableMock, complexTableMock, onDemandTableMock)
        );
        

        final List<RetentionPeriodTableEntity> result = RetentionPeriodUtils.collectTableLevelEntities(kpiDefinitionRequestMock, collectionId);

        assertThat(result).containsExactlyInAnyOrder(
                retentionPeriodTableEntity(collectionId, "kpi_alias_simple_60", 5),
                retentionPeriodTableEntity(collectionId, "kpi_alias_complex_60", 4)
        );
    }

    @Test
    void collectionLevelEntityTest(@Mock KpiDefinitionRequest kpiDefinitionRequestMock) {
        when(kpiDefinitionRequestMock.retentionPeriod()).thenReturn(RetentionPeriod.of(10));

        final Optional<RetentionPeriodCollectionEntity> result = RetentionPeriodUtils.collectCollectionLevelEntity(kpiDefinitionRequestMock, collectionId);

        assertThat(result).hasValue(retentionPeriodCollectionEntity(collectionId, 10));
    }

    @Test
    void collectionLevelEntityTestWhenNoRetentionPeriod() {
        when(kpiDefinitionRequestMock.retentionPeriod()).thenReturn(RetentionPeriod.of(null));
        final Optional<RetentionPeriodCollectionEntity> result = RetentionPeriodUtils.collectCollectionLevelEntity(kpiDefinitionRequestMock, collectionId);

        assertThat(result).isEmpty();

    }


    @Test
    void toTableLevelEntityTest() {
        when(simpleTableMock.tableName()).thenReturn("kpi_alias_simple_60");
        when(simpleTableMock.retentionPeriod()).thenReturn(SimpleTableRetentionPeriod.of(5));

        final RetentionPeriodTableEntity result = RetentionPeriodUtils.mapRetentionPeriodToTableLevelEntity(
                UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79"),
                simpleTableMock
        );

        assertThat(result).isEqualTo(
                RetentionPeriodTableEntity.builder()
                        .withRetentionPeriodInDays(5)
                        .withTableName("kpi_alias_simple_60")
                        .withKpiCollectionId(UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79"))
                        .build()
        );
    }

    @Test
    void toCollectionLevelEntity(@Mock KpiDefinitionRequest kpiDefinitionRequestMock) {
        when(kpiDefinitionRequestMock.retentionPeriod()).thenReturn(RetentionPeriod.of(10));

        final RetentionPeriodCollectionEntity result = RetentionPeriodUtils.mapRetentionPeriodCollectionLevelEntity(kpiDefinitionRequestMock, collectionId);

        assertThat(result).isEqualTo(
                RetentionPeriodCollectionEntity.builder()
                        .withRetentionPeriodInDays(10)
                        .withKpiCollectionId(collectionId)
                        .build()
        );
    }

    private static RetentionPeriodTableEntity retentionPeriodTableEntity(final UUID collectionId, final String tableName, final Integer retentionPeriod) {
        final RetentionPeriodTableEntityBuilder builder = RetentionPeriodTableEntity.builder();
        builder.withKpiCollectionId(collectionId);
        builder.withTableName(tableName);
        builder.withRetentionPeriodInDays(retentionPeriod);
        return builder.build();
    }

    private static RetentionPeriodCollectionEntity retentionPeriodCollectionEntity(final UUID collectionId, final Integer retentionPeriod) {
        final RetentionPeriodCollectionEntityBuilder builder = RetentionPeriodCollectionEntity.builder();
        builder.withKpiCollectionId(collectionId);
        builder.withRetentionPeriodInDays(retentionPeriod);
        return builder.build();
    }
}