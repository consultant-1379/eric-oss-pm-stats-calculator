/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.producer;

import static com.ericsson.oss.air.pm.stats.test.tools.KafkaFactories.kafkaProducerProperties;
import static com.ericsson.oss.air.pm.stats.test.tools.producer.TimeUtils.stringToLocalDate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.ericsson.oss.air.pm.stats.test.tools.KafkaFactories;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.CommonFact;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.FactKpiService;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.FactTableKpiService;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.FactTableTestNamespace;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.FactTestNamespace;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.LimitedAggregation;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.LimitedAggregationData;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.NewSimple;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.NewVerySimpleTable;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.SampleCell;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.SampleCellFact;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.SampleCellTDD;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.SampleCellTDDFact;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.SampleRelation;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.SampleRelationFact;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.SimpleSameDayTable;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.Counter;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.SampleCellCounters;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.SampleCellTDDCounters;
import com.ericsson.oss.air.pm.stats.test.tools.producer.facts.pmcounters.SampleRelationCounters;
import com.ericsson.oss.air.pm.stats.test.tools.schema.registry.SchemaUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringSerializer;

@Slf4j
public final class KafkaWriter {
    private final AdminClient adminClient = KafkaFactories.adminClient();
    private final Producer<String, GenericRecord> producerGen = new KafkaProducer<>(kafkaProducerProperties(
            StringSerializer.class, KafkaAvroSerializer.class
    ));

    /**
     * Write a file to a kafka topic.
     *
     * @param fileName             the name of the file to write from
     * @param topic                where the data should be written
     * @param numberOfPartitions the number of partition to create
     * @param avroSchemaFile the name of the schema file
     */
    public static void writeToKafka(String fileName, String topic, int numberOfPartitions, String avroSchemaFile) {
        if (fileName.endsWith(".json")) {
            KafkaWriter kafkaWriter = new KafkaWriter();
            kafkaWriter.createTopic(topic, numberOfPartitions);
            kafkaWriter.handleFile(fileName, topic, avroSchemaFile);
        } else {
            throw new IllegalStateException("Kafka Writer supports only JSON file types.");
        }
    }

    public static List<Integer> stringToIntList(String input) {
        return input == null ? null : Arrays.stream(input.replace("{", "")
                                                         .replace("}", "")
                                                         .split(","))
                                            .map(Integer::parseInt)
                                            .collect(Collectors.toList());
    }

    public static List<Double> stringToDoubleList(String input) {
        return input == null ? null : Arrays.stream(input.replace("{", "")
                                                         .replace("}", "")
                                                         .split(","))
                                            .map(Double::parseDouble)
                                            .collect(Collectors.toList());
    }

    private void createTopic(String topic, int numberOfPartitions) {
        try {
            final CreateTopicsResult result = adminClient.createTopics(Collections.singleton(new NewTopic(topic, numberOfPartitions, (short) 1)));

            // Call values() to get the result for a specific topic
            KafkaFuture<Void> future = result.values().get(topic);

            // Call get() to block until the topic creation is complete or has failed
            // if creation failed the ExecutionException wraps the underlying cause.
            future.get();
        } catch (InterruptedException e) {
            log.error("Interrupted exception");
            System.exit(1);
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof TopicExistsException)) {
                log.error("Execution exception" + e);
                System.exit(1);
            }
        }
    }

    private void handleFile(String fileName, String topic, String schemaFileName) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            log.info("fileName = " + fileName);

            final String data = String.join(System.lineSeparator(), Files.readAllLines(Path.of(fileName)));

            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            Schema simpleSchema = SchemaUtils.parseSchema(schemaFileName);

            log.info("Schema loaded successfully");

            if (fileName.contains(KafkaConstants.FACT_TABLE_TEST_NAMESPACE_JSON)) {
                final FactTableTestNamespace factTable0 = objectMapper.readValue(data, FactTableTestNamespace.class);
                writeAllFact0IntoKafkaTopicNewSchema(factTable0.getFacts(), simpleSchema, topic);
            } else if (fileName.contains(KafkaConstants.FACT_TABLE_KPI_SERVICE_JSON)) {
                final FactTableKpiService factTable2 = objectMapper.readValue(data, FactTableKpiService.class);
                writeAllFact2IntoKafkaTopicNewSchema(factTable2.getFacts(), simpleSchema, topic);
            } else if (fileName.contains(KafkaConstants.SIMPLE_KPI_SAME_DAY_JSON)) {
                final SimpleSameDayTable simpleTable = objectMapper.readValue(data, SimpleSameDayTable.class);
                writeAllVerySimpleIntoKafkaTopicNewSchema(simpleTable.getFacts(), simpleSchema, topic);
            } else if (fileName.contains(KafkaConstants.NEW_VERY_SIMPLE_KPI_JSON) || fileName.contains(KafkaConstants.NEW_VERY_SIMPLE_LATE_DATA_JSON)) {
                final NewVerySimpleTable simpleTable = objectMapper.readValue(data, NewVerySimpleTable.class);
                writeAllVerySimpleIntoKafkaTopicNewSchema(simpleTable.getFacts(), simpleSchema, topic);
            } else if (fileName.contains(KafkaConstants.LIMITED_AGGREGATION_JSON)) {
                final LimitedAggregationData limitedAggregationData = objectMapper.readValue(data, LimitedAggregationData.class);
                writeAllLimitedAggregationDataIntoKafkaTopic(limitedAggregationData.getFacts(), simpleSchema, topic);
            } else if(fileName.contains(KafkaConstants.SAMPLE_CELL_FDD_JSON)) {
                final SampleCell sampleCell = objectMapper.readValue(data, SampleCell.class);
                sendSampleCellFddData(sampleCell.getFacts(), simpleSchema, topic);
            } else if(fileName.contains(KafkaConstants.SAMPLE_CELL_TDD_JSON)) {
                final SampleCellTDD sampleCellTDD = objectMapper.readValue(data, SampleCellTDD.class);
                sendSampleCellTddData(sampleCellTDD.getFacts(), simpleSchema, topic);
            } else if (fileName.contains(KafkaConstants.SAMPLE_RELATION_JSON)) {
                final SampleRelation sampleRelation = objectMapper.readValue(data, SampleRelation.class);
                sendSampleRelationData(sampleRelation.getFacts(), simpleSchema, topic);
            }

            closeKafka();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        log.info("Data loading completed in: " + stopWatch);
    }

    private void writeAllFact0IntoKafkaTopicNewSchema(List<FactTestNamespace> facts, Schema simpleSchema, String topic) {
        for (final FactTestNamespace fact : facts) {
            LocalDateTime ropBeginTime = stringToLocalDate(fact.getRopBeginTime());
            ropBeginTime = TimeUtils.nowMinusDays(ropBeginTime, ropBeginTime.getDayOfMonth() == 11 ? 2 : 1).plusHours(2);
            fact.setRopBeginTime(ropBeginTime.toString());
            fact.setRopEndTime(ropBeginTime.plusMinutes(15).toString());

            final GenericRecord pmCountersRecord = new GenericData.Record(simpleSchema.getField("pmCounters").schema());
            pmCountersRecord.put("integerColumn0", fact.getPmCounters().getIntegerColumn0());

            final GenericRecord factRecord = new GenericData.Record(simpleSchema);
            factRecord.put("nodeFDN", fact.getNodeFDN());
            factRecord.put("ropBeginTime", fact.getRopBeginTime());
            factRecord.put("ropEndTime", fact.getRopEndTime());
            factRecord.put("pmCounters", pmCountersRecord);

            writeKafka(factRecord, topic);
        }
    }

    private void writeAllFact2IntoKafkaTopicNewSchema(List<FactKpiService> facts, Schema simpleSchema, String topic) {
        for (final FactKpiService fact : facts) {
            LocalDateTime ropBeginTime = stringToLocalDate(fact.getRopBeginTime());
            ropBeginTime = TimeUtils.nowMinusDays(ropBeginTime, ropBeginTime.getDayOfMonth() == 11 ? 2 : 1).plusHours(2);
            fact.setRopBeginTime(ropBeginTime.toString());
            fact.setRopEndTime(ropBeginTime.plusMinutes(15).toString());

            final GenericRecord pmCountersRecord = new GenericData.Record(simpleSchema.getField("pmCounters").schema());
            pmCountersRecord.put("integerArrayColumn0", stringToIntList(fact.getPmCounters().getIntegerArrayColumn0()));
            pmCountersRecord.put("integerArrayColumn1", stringToIntList(fact.getPmCounters().getIntegerArrayColumn1()));
            pmCountersRecord.put("integerArrayColumn2", stringToIntList(fact.getPmCounters().getIntegerArrayColumn2()));
            pmCountersRecord.put("floatArrayColumn0", stringToDoubleList(fact.getPmCounters().getFloatArrayColumn0()));
            pmCountersRecord.put("floatArrayColumn1", stringToDoubleList(fact.getPmCounters().getFloatArrayColumn1()));
            pmCountersRecord.put("floatArrayColumn2", stringToDoubleList(fact.getPmCounters().getFloatArrayColumn2()));
            pmCountersRecord.put("integerColumn0", fact.getPmCounters().getIntegerColumn0());
            pmCountersRecord.put("floatColumn0", fact.getPmCounters().getFloatColumn0());

            final GenericRecord factRecord = new GenericData.Record(simpleSchema);
            factRecord.put("nodeFDN", fact.getNodeFDN());
            factRecord.put("ropBeginTime", fact.getRopBeginTime());
            factRecord.put("ropEndTime", fact.getRopEndTime());
            factRecord.put("pmCounters", pmCountersRecord);

            writeKafka(factRecord, topic);
        }
    }

    private void writeAllVerySimpleIntoKafkaTopicNewSchema(List<NewSimple> facts, Schema simpleSchema, String topic) {
        for (final NewSimple fact : facts) {
            LocalDateTime ropBeginTime = stringToLocalDate(fact.getRopBeginTime());
            ropBeginTime = TimeUtils.nowMinusDays(ropBeginTime, ropBeginTime.getDayOfMonth() == 26 ? 2 : 1);
            fact.setRopBeginTime(ropBeginTime.toString());
            fact.setRopEndTime(ropBeginTime.plusMinutes(15).toString());

            final GenericRecord pmCountersRecord = new GenericData.Record(simpleSchema.getField("pmCounters").schema());
            pmCountersRecord.put("integerArrayColumn0", stringToIntList(fact.getPmCounters().getIntegerArrayColumn0()));
            pmCountersRecord.put("floatArrayColumn0", stringToDoubleList(fact.getPmCounters().getFloatArrayColumn0()));
            pmCountersRecord.put("integerColumn0", fact.getPmCounters().getIntegerColumn0());
            pmCountersRecord.put("floatColumn0", fact.getPmCounters().getFloatColumn0());

            final GenericRecord record = new GenericData.Record(simpleSchema);
            record.put("agg_column_0", fact.getAggColumn0());
            record.put("agg_column_1", fact.getAggColumn1());
            record.put("ropBeginTime", fact.getRopBeginTime());
            record.put("ropEndTime", fact.getRopEndTime());
            record.put("pmCounters", pmCountersRecord);

            writeKafka(record, topic);
        }
    }

    private void writeAllLimitedAggregationDataIntoKafkaTopic(List<LimitedAggregation> facts, Schema simpleSchema, String topic) {
        for (final LimitedAggregation fact : facts) {
            LocalDateTime ropBeginTime = stringToLocalDate(fact.getRopBeginTime());
            LocalDateTime ropEndTime = stringToLocalDate(fact.getRopEndTime());
            ropBeginTime = TimeUtils.nowMinusDays(ropBeginTime, 2);
            fact.setRopBeginTime(ropBeginTime.toString());
            fact.setRopEndTime(ropEndTime.toString());

            final GenericRecord pmCountersRecord = new GenericData.Record(simpleSchema.getField("pmCounters").schema());
            pmCountersRecord.put("integerColumn0", fact.getPmCounters().getIntegerColumn0());
            pmCountersRecord.put("integerColumn1", fact.getPmCounters().getIntegerColumn1());
            pmCountersRecord.put("integerArrayColumn", fact.getPmCounters().getIntegerArrayColumn());

            final GenericRecord record = new GenericData.Record(simpleSchema);
            record.put("ossID", fact.getOssID());
            record.put("ropBeginTime", fact.getRopBeginTime());
            record.put("ropEndTime", fact.getRopEndTime());
            record.put("pmCounters", pmCountersRecord);

            writeKafka(record, topic);
        }
    }

    private void sendSampleCellFddData(List<SampleCellFact> facts, Schema schema, String topic) {
        for (final SampleCellFact fact : facts) {
            LocalDateTime ropBeginTime = stringToLocalDate(fact.getRopBeginTime());
            ropBeginTime = TimeUtils.nowMinusDays(ropBeginTime, ropBeginTime.getDayOfMonth() == 28 ? 2 : 1);
            fact.setRopBeginTime(ropBeginTime.toString());
            fact.setRopEndTime(ropBeginTime.plusMinutes(15).toString());

            SampleCellCounters sampleCellCounters = fact.getPmCounters();

            final GenericRecord exampleCounterRecord = createCounterRecord(sampleCellCounters.getExampleCounter(), schema, "exampleCounter");
            final GenericRecord exampleCounter1Record = createCounterRecord(sampleCellCounters.getExampleCounter1(), schema, "exampleCounter1");
            final GenericRecord exampleCounter2Record = createCounterRecord(sampleCellCounters.getExampleCounter2(), schema, "exampleCounter2");
            final GenericRecord exampleCounter3Record = createCounterRecord(sampleCellCounters.getExampleCounter3(), schema, "exampleCounter3");
            final GenericRecord exampleCounter4Record = createCounterRecord(sampleCellCounters.getExampleCounter4(), schema, "exampleCounter4");
            final GenericRecord exampleCounter5Record = createCounterRecord(sampleCellCounters.getExampleCounter5(), schema, "exampleCounter5");
            final GenericRecord exampleCounter6Record = createCounterRecord(sampleCellCounters.getExampleCounter6(), schema, "exampleCounter6");
            final GenericRecord pmExampleCounter1Record = createCounterRecord(sampleCellCounters.getPmExampleCounter1(), schema, "pmExampleCounter1");
            final GenericRecord pmExampleCounter2Record = createCounterRecord(sampleCellCounters.getPmExampleCounter2(), schema, "pmExampleCounter2");
            final GenericRecord pdfCounterRecord = createCounterRecord(sampleCellCounters.getPdfCounter(), schema, "pdfCounter");
            final GenericRecord exampleCompressedRecord = createCounterRecord(sampleCellCounters.getExampleCompressed(), schema, "exampleCompressed");

            final GenericRecord pmCountersRecord = new GenericData.Record(schema.getField("pmCounters").schema());
            pmCountersRecord.put("exampleCounter", exampleCounterRecord);
            pmCountersRecord.put("exampleCounter1", exampleCounter1Record);
            pmCountersRecord.put("exampleCounter2", exampleCounter2Record);
            pmCountersRecord.put("exampleCounter3", exampleCounter3Record);
            pmCountersRecord.put("exampleCounter4", exampleCounter4Record);
            pmCountersRecord.put("exampleCounter5", exampleCounter5Record);
            pmCountersRecord.put("exampleCounter6", exampleCounter6Record);
            pmCountersRecord.put("pmExampleCounter1", pmExampleCounter1Record);
            pmCountersRecord.put("pmExampleCounter2", pmExampleCounter2Record);
            pmCountersRecord.put("pdfCounter", pdfCounterRecord);
            pmCountersRecord.put("exampleCompressed", exampleCompressedRecord);


            final GenericRecord factRecord = createCommonFactRecord(fact, schema);
            factRecord.put("pmCounters", pmCountersRecord);

            writeKafka(factRecord, topic);
        }
    }

    private void sendSampleCellTddData(List<SampleCellTDDFact> facts, Schema schema, String topic) {
        for (final SampleCellTDDFact fact : facts) {
            LocalDateTime ropBeginTime = stringToLocalDate(fact.getRopBeginTime());
            ropBeginTime = TimeUtils.nowMinusDays(ropBeginTime, ropBeginTime.getDayOfMonth() == 28 ? 2 : 1);
            fact.setRopBeginTime(ropBeginTime.toString());
            fact.setRopEndTime(ropBeginTime.plusMinutes(15).toString());

            SampleCellTDDCounters sampleCellTDDCounters = fact.getPmCounters();

            final GenericRecord exampleCounterRecord = createCounterRecord(sampleCellTDDCounters.getExampleCounter(), schema, "exampleCounter");
            final GenericRecord exampleCounter1Record = createCounterRecord(sampleCellTDDCounters.getExampleCounter1(), schema, "exampleCounter1");
            final GenericRecord exampleCounter2Record = createCounterRecord(sampleCellTDDCounters.getExampleCounter2(), schema, "exampleCounter2");

            final GenericRecord pmCountersRecord = new GenericData.Record(schema.getField("pmCounters").schema());
            pmCountersRecord.put("exampleCounter", exampleCounterRecord);
            pmCountersRecord.put("exampleCounter1", exampleCounter1Record);
            pmCountersRecord.put("exampleCounter2", exampleCounter2Record);

            final GenericRecord factRecord = createCommonFactRecord(fact, schema);
            factRecord.put("pmCounters", pmCountersRecord);

            writeKafka(factRecord, topic);
        }
    }

    private void sendSampleRelationData(List<SampleRelationFact> facts, Schema schema, String topic) {
        for (final SampleRelationFact fact : facts) {
            LocalDateTime ropBeginTime = stringToLocalDate(fact.getRopBeginTime());
            ropBeginTime = TimeUtils.nowMinusDays(ropBeginTime, ropBeginTime.getDayOfMonth() == 28 ? 1 : 0);
            fact.setRopBeginTime(ropBeginTime.toString());
            fact.setRopEndTime(ropBeginTime.plusMinutes(15).toString());

            SampleRelationCounters sampleRelationCounters = fact.getPmCounters();

            final GenericRecord singleCounterRecord = createCounterRecord(sampleRelationCounters.getPmExampleSingleCounter(), schema, "pmExampleSingleCounter");
            final GenericRecord pdfCounterRecord = createCounterRecord(sampleRelationCounters.getPmExamplePdfCounter(), schema, "pmExamplePdfCounter");
            final GenericRecord pdfCompressedCounterRecord = createCounterRecord(sampleRelationCounters.getPmExampleCompressedPdfCounter(), schema, "pmExampleCompressedPdfCounter");

            final GenericRecord pmCountersRecord = new GenericData.Record(schema.getField("pmCounters").schema());
            pmCountersRecord.put("pmExampleSingleCounter", singleCounterRecord);
            pmCountersRecord.put("pmExamplePdfCounter", pdfCounterRecord);
            pmCountersRecord.put("pmExampleCompressedPdfCounter", pdfCompressedCounterRecord);

            final GenericRecord factRecord = createCommonFactRecord(fact, schema);
            factRecord.put("pmCounters", pmCountersRecord);

            writeKafka(factRecord, topic);
        }
    }

    private GenericRecord createCommonFactRecord(CommonFact fact, Schema schema) {
        final GenericRecord factRecord = new GenericData.Record(schema);
        factRecord.put("nodeFDN", fact.getNodeFDN());
        factRecord.put("elementType", fact.getElementType());
        factRecord.put("ropBeginTime", fact.getRopBeginTime());
        factRecord.put("ropEndTime", fact.getRopEndTime());
        factRecord.put("ropBeginTimeInEpoch", fact.getRopBeginTimeInEpoch());
        factRecord.put("ropEndTimeInEpoch", fact.getRopEndTimeInEpoch());
        factRecord.put("dnPrefix", fact.getDnPrefix());
        factRecord.put("moFdn", fact.getMoFdn());
        factRecord.put("suspect", fact.getSuspect());

        return factRecord;
    }

    private GenericRecord createCounterRecord(Counter counter, Schema schema, String field) {
        final GenericRecord record = new GenericData.Record(schema.getField("pmCounters").schema().getField(field).schema());
        record.put("counterType", counter.getCounterType());
        record.put("counterValue", counter.getCounterValue());
        record.put("isValuePresent", counter.getIsValuePresent());
        return record;
    }

    private void writeKafka(GenericRecord record, String topic) {
        ProducerRecord<String, GenericRecord> rec = new ProducerRecord<>(topic, record);
        rec.headers().add("schemaSubject", record.getSchema().getFullName().getBytes(StandardCharsets.UTF_8));
        try {
            producerGen.send(rec).get();
        } catch (final InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void closeKafka() {
        producerGen.flush();
        producerGen.close();
    }
}
