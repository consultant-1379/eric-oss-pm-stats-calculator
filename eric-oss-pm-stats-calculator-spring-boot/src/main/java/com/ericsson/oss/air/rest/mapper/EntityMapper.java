/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.rest.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.calculator.api.rest.v1.ReadinessLogResponse;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity;
import com.ericsson.oss.air.pm.stats.model.entity.KpiDefinitionEntity.KpiDefinitionEntityBuilder;
import com.ericsson.oss.air.pm.stats.model.entity.ReadinessLog;
import com.ericsson.oss.air.pm.stats.service.util.CollectionHelpers;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodManager;
import com.ericsson.oss.air.pm.stats.service.validator.retention.RetentionPeriodMemoizer;
import com.ericsson.oss.air.rest.output.KpiDefinitionDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionUpdateResponse;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.ComplexKpiDefinitionDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandKpiDefinitionDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandParameterDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandParameterDto.OnDemandParameterDtoBuilder;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandTableListDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandTabularParameterDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.OnDemandTabularParameterDto.OnDemandTabularParameterDtoBuilder;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.SimpleKpiDefinitionDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.TableDto;
import com.ericsson.oss.air.rest.output.KpiDefinitionsResponse.TableListDto;

import kpi.model.ondemand.OnDemandParameter;
import kpi.model.ondemand.OnDemandTabularParameter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EntityMapper {

    private final RetentionPeriodManager retentionPeriodManager;

    public Collection<ReadinessLogResponse> mapReadinessLogs(final Collection<? extends ReadinessLog> readinessLogs) {
        return CollectionHelpers.transform(readinessLogs, this::mapReadinessLog);
    }

    public ReadinessLogResponse mapReadinessLog(@NonNull final ReadinessLog readinessLog) {
        return ReadinessLogResponse.builder()
                                   .withCollectedRowsCount(readinessLog.getCollectedRowsCount())
                                   .withDatasource(readinessLog.getDatasource())
                                   .withEarliestCollectedData(readinessLog.getEarliestCollectedData())
                                   .withLatestCollectedData(readinessLog.getLatestCollectedData())
                                   .build();
    }

    public KpiDefinitionsResponse mapToKpiDefinitionResponse(
            @NonNull final List<KpiDefinitionEntity> kpiDefinitionEntities,
            final List<OnDemandTabularParameter> onDemandTabularParameters,
            final List<OnDemandParameter> onDemandParameters
    ) {
        final List<TableDto> simpleTables = new ArrayList<>();
        final List<TableDto> complexTables = new ArrayList<>();
        final List<TableDto> onDemandTables = new ArrayList<>();

        final RetentionPeriodMemoizer retentionPeriodMemoizer = retentionPeriodManager.retentionPeriodMemoizer();

        for (final KpiDefinitionEntity kpiDefinitionEntity : kpiDefinitionEntities) {
            if (kpiDefinitionEntity.isSimple()) {
                simpleTables.stream()
                            .filter(table -> areTableNamesEqual(table, kpiDefinitionEntity))
                            .findFirst()
                            .ifPresentOrElse(
                                    table -> table.getKpiDefinitions().add(mapToKpiDefinitionDto(kpiDefinitionEntity)),
                                    () -> simpleTables.add(mapToTableDto(kpiDefinitionEntity, retentionPeriodMemoizer)));
            } else if (kpiDefinitionEntity.isComplex()) {
                complexTables.stream()
                             .filter(table -> areTableNamesEqual(table, kpiDefinitionEntity))
                             .findFirst()
                             .ifPresentOrElse(
                                     table -> table.getKpiDefinitions().add(mapToKpiDefinitionDto(kpiDefinitionEntity)),
                                     () -> complexTables.add(mapToTableDto(kpiDefinitionEntity, retentionPeriodMemoizer)));
            } else {
                onDemandTables.stream()
                              .filter(table -> areTableNamesEqual(table, kpiDefinitionEntity))
                              .findFirst()
                              .ifPresentOrElse(
                                      table -> table.getKpiDefinitions().add(mapToKpiDefinitionDto(kpiDefinitionEntity)),
                                      () -> onDemandTables.add(mapToTableDto(kpiDefinitionEntity, retentionPeriodMemoizer)));
            }
        }

        final TableListDto scheduledSimple = new TableListDto(simpleTables);
        final TableListDto scheduledComplex = new TableListDto(complexTables);
        final List<OnDemandTabularParameterDto> onDemandTabularParameterDtoList = onDemandTabularParameters.stream()
                .map(this::mapToOnDemandTabularParameterDto)
                .collect(Collectors.toList());

        final List<OnDemandParameterDto> onDemandParameterDtoList=onDemandParameters.stream()
                .map(this::mapToOnDemandParameterDto)
                .collect(Collectors.toList());
        final OnDemandTableListDto onDemand = new OnDemandTableListDto(onDemandParameterDtoList, onDemandTabularParameterDtoList, onDemandTables);
        return new KpiDefinitionsResponse(scheduledSimple, scheduledComplex, onDemand);
    }

    public KpiDefinitionUpdateResponse mapToKpiDefinitionUpdateResponse(@NonNull final KpiDefinitionEntity kpiDefinitionEntity) {
        final KpiDefinitionUpdateResponse.KpiDefinitionUpdateResponseBuilder builder = KpiDefinitionUpdateResponse.builder()
                .name(kpiDefinitionEntity.name())
                .alias(kpiDefinitionEntity.alias())
                .aggregationPeriod(kpiDefinitionEntity.aggregationPeriod())
                .expression(kpiDefinitionEntity.expression())
                .objectType(kpiDefinitionEntity.objectType())
                .aggregationType(kpiDefinitionEntity.aggregationType())
                .aggregationElements(kpiDefinitionEntity.aggregationElements())
                .exportable(kpiDefinitionEntity.exportable())
                .filters(kpiDefinitionEntity.filters());

        if (!kpiDefinitionEntity.isOnDemand()) {
            builder.reexportLateData(kpiDefinitionEntity.reexportLateData())
                    .dataReliabilityOffset(kpiDefinitionEntity.dataReliabilityOffset())
                    .dataLookbackLimit(kpiDefinitionEntity.dataLookbackLimit());
        }

        if (kpiDefinitionEntity.isSimple()) {
            builder.inpDataIdentifier(Objects.requireNonNull(kpiDefinitionEntity.dataIdentifier()).getName());
        } else if (kpiDefinitionEntity.isComplex()) {
            builder.executionGroup(kpiDefinitionEntity.executionGroup().name());
        }

        return builder.build();
    }

    private KpiDefinitionDto mapToKpiDefinitionDto(@NonNull final KpiDefinitionEntity kpiDefinitionEntity) {
        if (kpiDefinitionEntity.isSimple()) {
            return SimpleKpiDefinitionDto.builder()
                                         .name(kpiDefinitionEntity.name())
                                         .expression(kpiDefinitionEntity.expression())
                                         .objectType(kpiDefinitionEntity.objectType())
                                         .aggregationType(kpiDefinitionEntity.aggregationType())
                                         .aggregationElements(kpiDefinitionEntity.aggregationElements())
                                         .exportable(kpiDefinitionEntity.exportable())
                                         .filters(kpiDefinitionEntity.filters())
                                         .reexportLateData(kpiDefinitionEntity.reexportLateData())
                                         .dataReliabilityOffset(kpiDefinitionEntity.dataReliabilityOffset())
                                         .dataLookbackLimit(kpiDefinitionEntity.dataLookbackLimit())
                                         .inpDataIdentifier(Objects.requireNonNull(kpiDefinitionEntity.dataIdentifier()).getName())
                                         .build();

        } else if (kpiDefinitionEntity.isComplex()) {
            return ComplexKpiDefinitionDto.builder()
                                          .name(kpiDefinitionEntity.name())
                                          .expression(kpiDefinitionEntity.expression())
                                          .objectType(kpiDefinitionEntity.objectType())
                                          .aggregationType(kpiDefinitionEntity.aggregationType())
                                          .aggregationElements(kpiDefinitionEntity.aggregationElements())
                                          .exportable(kpiDefinitionEntity.exportable())
                                          .filters(kpiDefinitionEntity.filters())
                                          .reexportLateData(kpiDefinitionEntity.reexportLateData())
                                          .dataReliabilityOffset(kpiDefinitionEntity.dataReliabilityOffset())
                                          .dataLookbackLimit(kpiDefinitionEntity.dataLookbackLimit())
                                          .executionGroup(kpiDefinitionEntity.executionGroup().name())
                                          .build();
        }

        return OnDemandKpiDefinitionDto.builder()
                                       .name(kpiDefinitionEntity.name())
                                       .expression(kpiDefinitionEntity.expression())
                                       .objectType(kpiDefinitionEntity.objectType())
                                       .aggregationType(kpiDefinitionEntity.aggregationType())
                                       .aggregationElements(kpiDefinitionEntity.aggregationElements())
                                       .exportable(kpiDefinitionEntity.exportable())
                                       .filters(kpiDefinitionEntity.filters())
                                       .build();

    }

    private TableDto mapToTableDto(@NonNull final KpiDefinitionEntity kpiDefinitionEntity, final RetentionPeriodMemoizer retentionPeriodMemoizer) {
            final int retentionPeriod = retentionPeriodMemoizer.computeRetentionPeriod(kpiDefinitionEntity);
            final ArrayList<KpiDefinitionDto> kpiDefinitions = new ArrayList<>();
            kpiDefinitions.add(mapToKpiDefinitionDto(kpiDefinitionEntity));

            return new TableDto(kpiDefinitionEntity.alias(), kpiDefinitionEntity.aggregationPeriod(), retentionPeriod, kpiDefinitions);
    }

    private boolean areTableNamesEqual(@NonNull final TableDto tableDto, @NonNull final KpiDefinitionEntity kpiDefinitionEntity) {
        final String entityTableName = kpiDefinitionEntity.tableName();
        final String dtoTableName = toEntity(tableDto).tableName();

        return dtoTableName.equals(entityTableName);
    }

    private static KpiDefinitionEntity toEntity(@NonNull final TableDto tableDto) {
        final KpiDefinitionEntityBuilder builder = KpiDefinitionEntity.builder();
        builder.withAlias(tableDto.getAlias());
        builder.withAggregationPeriod(tableDto.getAggregationPeriod());
        return builder.build();
    }

    private OnDemandTabularParameterDto mapToOnDemandTabularParameterDto(final OnDemandTabularParameter onDemandTabularParameter){
        final OnDemandTabularParameterDtoBuilder builder = OnDemandTabularParameterDto.builder();
        builder.name(onDemandTabularParameter.name());
        builder.columns(onDemandTabularParameter.columns().stream().map(this::mapToOnDemandParameterDto).collect(Collectors.toList()));
        return builder.build();
    }

    private OnDemandParameterDto mapToOnDemandParameterDto(final OnDemandParameter onDemandParameter) {
        final OnDemandParameterDtoBuilder builder = OnDemandParameterDto.builder();
        builder.name(onDemandParameter.name());
        builder.type(onDemandParameter.type().name());
        return builder.build();
    }
}
