/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.exporter.util;

import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorErrorCode;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.KpiCalculatorException;
import com.ericsson.oss.air.pm.stats.service.exporter.model.ExecutionReport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Class used for serializing JSON messages to be sent to Kafka.
 */
@Slf4j
public class KafkaJsonSerializer implements Serializer<ExecutionReport> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(final String topic, final ExecutionReport data) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            log.error("Failed serialize ExecutionReport");
            throw new KpiCalculatorException(KpiCalculatorErrorCode.KPI_SENDING_EXECUTION_REPORT_ERROR, e);
        }
    }

}
