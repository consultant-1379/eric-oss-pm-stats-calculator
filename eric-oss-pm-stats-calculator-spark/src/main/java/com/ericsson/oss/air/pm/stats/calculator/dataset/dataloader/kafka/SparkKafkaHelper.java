/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka;

import static com.ericsson.oss.air.pm.stats.common.util.LocalDateTimeTruncates.truncateToAggregationPeriod;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.column;
import static org.apache.spark.sql.functions.min;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.spark.SparkTimestampAdjuster;
import com.ericsson.oss.air.pm.stats.calculator.model.TableDataset;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.ReadinessLogRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.api.SparkService;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.util.sql.SqlProcessorDelegator;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath;
import com.ericsson.oss.air.pm.stats.common.sqlparser.domain.JsonPath.Part;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import za.co.absa.abris.config.FromAvroConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class SparkKafkaHelper {
    public static final String AGGREGATION_BEGIN_TIME = "aggregation_begin_time";

    private final SparkService sparkService;
    private final SparkTimestampAdjuster sparkTimestampAdjuster;
    private final KpiDefinitionHelperImpl kpiDefinitionHelper;
    private final ReadinessLogRepository readinessLogRepository;
    private final SqlProcessorDelegator sqlProcessorDelegator;
    private final CalculationProperties calculationProperties;

    /**
     * Loads data from a given <strong>Kafka</strong> topic by using <strong>Spark</strong>.
     *
     * @param schemaDetail
     *         {@link SchemaDetail} containing information on Kafka.
     * @param startingOffset
     *         starting offset to read messages from.
     * @param endingOffset
     *         ending offset to read messages to.
     * @return loaded {@link Dataset}.
     */
    public Dataset<Row> loadDataFrom(@NonNull final SchemaDetail schemaDetail, final String startingOffset, final String endingOffset) {

        return sparkService.fromKafka()
                           .option("kafka.bootstrap.servers", calculationProperties.getKafkaBootstrapServers())
                           .option("subscribe", schemaDetail.getTopic())
                           .option("includeHeaders", true)
                           .option("startingOffsets", startingOffset)
                           .option("endingOffsets", endingOffset)
                           .load();
    }

    /**
     * Processes data coming from <strong>Kafka</strong> topic.
     * <br>
     * It targets the messages <strong>value</strong> column.
     *
     * @param dataset
     *         {@link Dataset} to process.
     * @param avroConfig
     *         {@link FromAvroConfig} storing the schema to read <strong>AVRO</strong> encoded data.
     * @return {@link Dataset} containing processed data.
     */
    public Dataset<Row> processData(@NonNull final Dataset<Row> dataset, final FromAvroConfig avroConfig) {
        return dataset.select(za.co.absa.abris.avro.functions.from_avro(col("value"), avroConfig).as("data"))
                      .select(col("data.*"));
    }

    public Dataset<Row> convertRopToTimestampColumns(final Dataset<Row> dataset) {
        Dataset<Row> adjustedDataset = dataset.withColumn("ropBeginTime", sparkTimestampAdjuster.adjustStringToTimestampColumn("ropBeginTime"))
                .withColumn("ropEndTime", sparkTimestampAdjuster.adjustStringToTimestampColumn("ropEndTime"));
        return adjustedDataset.withColumnRenamed("ropBeginTime", AGGREGATION_BEGIN_TIME);
    }

    public Seq<org.apache.spark.sql.Column> computeProjectionColumns(final DataIdentifier dataIdentifier, final Collection<KpiDefinition> kpiDefinitions) {
        final Set<org.apache.spark.sql.Column> result = new HashSet<>();

        for (final KpiDefinition kpiDefinition : kpiDefinitions) {
            if (!kpiDefinition.hasSameDataIdentifier(dataIdentifier)) {
                continue;
            }

            result.addAll(collectExpressionColumns(kpiDefinition));
            result.addAll(collectAggregationElementColumns(kpiDefinition));
        }

        //  Enrich projection with timestamp field
        result.add(column(AGGREGATION_BEGIN_TIME));

        removeDuplicates(result);

        return JavaConverters.asScalaSetConverter(result).asScala().toSeq();
    }

    public Dataset<Row> applyDataLookBackFilter(final Dataset<Row> dataset, final Collection<KpiDefinition> kpiDefinitions) {
        KpiDefinition kpiDefinition = IterableUtils.first(kpiDefinitions);

        final Integer dataLookbackLimit = kpiDefinitionHelper.extractDataLookbackLimit(kpiDefinitions);
        return readinessLogRepository
                .findLatestCollectedTimeByDataSourceAndExecutionGroup(kpiDefinition.datasource(), kpiDefinition.getExecutionGroup())
                .map(latestCollectedData -> latestCollectedData.minusMinutes(dataLookbackLimit))
                .map(limit -> dataset.filter(col(AGGREGATION_BEGIN_TIME).geq(Timestamp.valueOf(limit))))
                .orElse(dataset);
    }

    public Optional<LocalDateTime> findEarliestTruncatedDateLoadedFromKafka(final TableDataset tableDataset, final Integer aggregationPeriod) {
        final Timestamp earliestAggregationBeginTime = (Timestamp) tableDataset.getDataset().agg(min(AGGREGATION_BEGIN_TIME)).head().get(0);
        return ofNullable(earliestAggregationBeginTime).map(aggregationBeginTime -> truncateToAggregationPeriod(
                aggregationBeginTime.toLocalDateTime(), aggregationPeriod
        ));
    }

    private Collection<org.apache.spark.sql.Column> collectExpressionColumns(final KpiDefinition kpiDefinition) {
        final Set<org.apache.spark.sql.Column> result = new HashSet<>();

        for (final JsonPath jsonPath : sqlProcessorDelegator.jsonPaths(kpiDefinition.getExpression())) {
            final Deque<String> parts = jsonPath.parts().stream().map(Part::name).collect(toCollection(LinkedList::new));

            // The first part is skipped as it is the `name` of the schema - it is not a `field` in the schema
            parts.removeFirst();

            // If parts only contains 1 part after the schema remove, that means we don't need to make an alias for it. That would just do nodeFDN as nodeFDN
            // Which could cause trouble later on, if we add the same from an aggregation element.
            // Then spark would have a nodeFDN as nodeFDN and a nodeFDN column and couldn't decide which to use.
            if (parts.size() == 1) {
                result.add(column(parts.getFirst()));
            } else {

                // Spark explodes nested AVRO schemas to '.' separated objects
                // We unfold by aliasing them with '_' seperated columns
                //  <schema_name>.pmCounters.integerColumn0 --explode--> pmCounters.integerColumn0 AS pmCounters_integerColumn0
                final String columnName = String.join(".", parts);
                final String aliasName = String.join("_", parts);
                result.add(column(columnName).as(aliasName));
            }
        }

        return result;
    }

    private Collection<org.apache.spark.sql.Column> collectAggregationElementColumns(final KpiDefinition kpiDefinition) {
        final Set<org.apache.spark.sql.Column> result = new HashSet<>();

        sqlProcessorDelegator.aggregationElements(kpiDefinition.getAggregationElements()).forEach(reference -> {
            final String columnName = reference.requiredColumn().getName();
            reference.alias().ifPresentOrElse(
                    alias -> result.add(column(columnName).as(alias.name())),
                    () -> result.add(column(columnName))
            );
        });

        return result;
    }

    private static void removeDuplicates(final Collection<Column> result) {
        // org.apache.spark.sql.catalyst.expressions.Alias.equals considers exprId that is random UUID thus Set implementation for Alias is not
        // working as it is not eliminating two identical ones
        //     a AS b[exprId: "7b98f575-1f41-4285-933e-9ba07633f6b9"] != a AS b[exprId: "aee9c350-ba4a-4638-9b52-51fcb9e047b0"]

        final Map<String, Long> frequencies = result.stream().collect(groupingBy(Column::toString, HashMap::new, counting()));

        result.removeIf(column -> {
            final Long frequency = frequencies.get(column.toString());
            final boolean remove = frequency > 1;

            if (remove) {
                log.warn("Column '{}' removed as it has frequency '{}'", column, frequency);
                frequencies.replace(column.toString(), frequency - 1);
            }

            return remove;
        });
    }

}
