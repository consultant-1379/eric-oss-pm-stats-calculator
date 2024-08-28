/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.test.tools.consumer.serializer;

import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;


/**
 * Class used for deserializing JSON messages received from Kafka to {@link ExecutionReport}.
 */
@Slf4j
public class ExecutionReportDeserializer implements Deserializer<ExecutionReport> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Implementation of deserialize() method of Apache Kafka {@link Deserializer}.
     *
     * @param topic
     *         topic associated with the data
     * @param bytes
     *         serialized bytes
     * @return deserialized {@link ExecutionReport} data
     */
    @Override
    @SneakyThrows
    public ExecutionReport deserialize(final String topic, final byte[] bytes) {
        return OBJECT_MAPPER.readValue(bytes, ExecutionReport.class);
    }
}
