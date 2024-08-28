/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka;

import static org.apache.spark.sql.functions.array_contains;
import static org.apache.spark.sql.functions.col;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaRegistryException;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.ReadinessLogRegisterImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.offset.OffsetCalculatorFacadeImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;
import za.co.absa.abris.config.FromAvroConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class SparkKafkaLoader {
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final AvroSchemaFetcher avroSchemaFetcher;
    private final SparkKafkaHelper sparkKafkaHelper;
    private final OffsetCalculatorFacadeImpl offsetCalculator;
    private final ReadinessLogRegisterImpl readinessLogRegister;
    private final SparkService sparkService;

    /**
     * Loads data from Kafka using Avro schema.
     *
     * @param sourceIdentifier data identifier
     * @param filter filter
     * @param kpiDefinitions collection of KPI definitions
     * @param startingOffset starting Kafka offset
     * @param endingOffset ending Kafka offset
     * @return dataset
     * @throws SchemaRegistryException if fails to retrieve Avro schema
     */
    public TableDataset loadTableDataset(final DataIdentifier sourceIdentifier,
                                         final String filter,
                                         final Collection<KpiDefinition> kpiDefinitions,
                                         final String startingOffset,
                                         final String endingOffset) {
        final SchemaDetail schemaDetail = kpiDefinitionHelper.getSchemaDetailBy(kpiDefinitions, sourceIdentifier);
        final String topic = schemaDetail.getTopic();
        final String schemaName = sourceIdentifier.schema();
        final String namespace = schemaDetail.getNamespace();

        log.info("Getting avro schema {} under {} namespace for topic {}", schemaName, namespace, topic);
        FromAvroConfig avroConfig = avroSchemaFetcher.fetchAvroSchema(schemaName, namespace);

        /* TODO: Add proper logging for offsets in which we can clearly see that for partition 5 we read from offset 5 to offset 10 etc..
         *   Handle also if we use earliest and latest */
        log.info("The starting offset for this batch for topic {} is: {}", topic, startingOffset);
        log.info("The ending offset for this batch for topic {} is: {}", topic, endingOffset);

        final Dataset<Row> load = sparkKafkaHelper.loadDataFrom(schemaDetail, startingOffset, endingOffset);

        final Integer aggregationPeriod = kpiDefinitionHelper.extractAggregationPeriod(kpiDefinitions);

        //  For filtered Simple calculations end offsets are not defined
        //  TODO: this is only a temporal solution as filters for Simple execution groups are not supported at the moment
        if ("latest".equals(endingOffset)) {
            offsetCalculator.generateOffsetsListAndStore(load, topic, aggregationPeriod);
        } else {
            offsetCalculator.generateOffsetsListAndStore(topic, aggregationPeriod, endingOffset);
        }

        final String subject = String.format("%s.%s", namespace, schemaName);
        final Dataset<Row> filteredByHeader = load.filter(array_contains(col("headers.value"), subject.getBytes(StandardCharsets.UTF_8)));

        Dataset<Row> collectedData = sparkKafkaHelper.processData(filteredByHeader, avroConfig);
        collectedData = sparkKafkaHelper.convertRopToTimestampColumns(collectedData);
        collectedData = sparkKafkaHelper.applyDataLookBackFilter(collectedData, kpiDefinitions);

        //  Selection on what rows should be used
        final Dataset<Row> filteredDataset = filter.isEmpty() ? collectedData : collectedData.filter(filter);

        final Dataset<Row> sourceDataset = filteredDataset.select(sparkKafkaHelper.computeProjectionColumns(sourceIdentifier, kpiDefinitions));

        readinessLogRegister.registerReadinessLog(collectedData, kpiDefinitions);

        return sparkService.cacheView(schemaName, sourceDataset);
    }
}
