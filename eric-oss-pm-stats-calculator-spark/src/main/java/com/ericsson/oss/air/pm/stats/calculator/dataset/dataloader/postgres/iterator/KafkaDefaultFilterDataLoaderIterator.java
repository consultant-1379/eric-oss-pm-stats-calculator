/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.SparkKafkaHelper;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.SparkKafkaLoader;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.PostgresDataLoaderFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDatasets;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetCalculationHelper;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetCalculatorFacadeImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.Topic;
import com.ericsson.oss.air.pm.stats.common.spark.ReadingOptions;
import com.ericsson.oss.air.pm.stats.model.KpiDefinitionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KafkaDefaultFilterDataLoaderIterator implements Iterator<TableDatasets> {
    private final PostgresDataLoaderFacadeImpl postgresDataLoaderFacade;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final SparkService sparkService;
    private final OffsetCalculatorFacadeImpl offsetCalculator;
    private final OffsetCalculationHelper offsetCalculationHelper;
    private final SparkKafkaLoader sparkKafkaLoader;
    private final Collection<KpiDefinition> kpiDefinitions;
    private final SparkKafkaHelper sparkKafkaHelper;

    private final Map<Topic, Boolean> topicFinishedStatus = new HashMap<>();

    private boolean isLast;

    @Override
    public boolean hasNext() {
        return !isLast;
    }

    @Override
    public TableDatasets next() {
        try {
            final TableDatasets tableDatasets = TableDatasets.of();
            final ReadingOptions readingOptions = ReadingOptions.empty();

            final Integer aggregationPeriod = kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions);

            for (final DataIdentifier identifier : KpiDefinitionHandler.getDataIdentifiersFromKpiDefinitions(kpiDefinitions)) {
                final SchemaDetail schemaDetail = kpiDefinitionHelper.getSchemaDetailBy(kpiDefinitions, identifier);
                final Topic topic = Topic.of(schemaDetail.getTopic());
                topicFinishedStatus.putIfAbsent(topic, false);

                /* TODO: Reorganize code to serialize offsets only before needed so we do not need to deserialize */
                final String startingOffset = offsetCalculator.defineStartingOffset(topic, aggregationPeriod);
                final String endingOffset = offsetCalculator.defineEndOffset(topic, aggregationPeriod);

                if (offsetCalculationHelper.isLastRead(topic, endingOffset)) {
                    topicFinishedStatus.put(topic, true);
                }

                final TableDataset loadFromKafka = sparkKafkaLoader.loadTableDataset(identifier, EMPTY, kpiDefinitions, startingOffset, endingOffset);

                //  Default aggregation period data has no aggregation_begin_time column
                if (aggregationPeriod != DEFAULT_AGGREGATION_PERIOD_INT) {
                    sparkKafkaHelper.findEarliestTruncatedDateLoadedFromKafka(loadFromKafka, aggregationPeriod).ifPresentOrElse(earliestBeginTime -> {
                        log.info(
                                "For '{}' with aggregation period '{}' the earliest truncated period on Kafka is '{}'",
                                identifier, aggregationPeriod, earliestBeginTime
                        );
                        readingOptions.setAggregationBeginTimeLimit(earliestBeginTime);
                    }, () -> log.info("For '{}' with aggregation period '{}' found no earliest truncated period on Kafka", identifier, aggregationPeriod));
                }

                tableDatasets.put(loadFromKafka);
            }

            if (tableDatasets.hasNoEmptyValue()) {
                tableDatasets.putAll(postgresDataLoaderFacade.loadDataset(kpiDefinitions, readingOptions));
            }

            if (!topicFinishedStatus.containsValue(false)) {
                isLast = true;
            }

            return tableDatasets;
        } catch (final Exception e) {
            log.error("Error loading datasets for aggregation period: {}", kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions));
            throw new KpiCalculatorException(KpiCalculatorErrorCode.KPI_CALCULATION_ERROR, e);
        }
    }

}
