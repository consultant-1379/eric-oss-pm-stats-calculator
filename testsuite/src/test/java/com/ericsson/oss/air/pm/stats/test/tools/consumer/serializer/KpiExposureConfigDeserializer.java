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

import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiExposureConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;

public class KpiExposureConfigDeserializer implements Deserializer<KpiExposureConfig> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @SneakyThrows
    public KpiExposureConfig deserialize(String s, byte[] bytes) {
        return OBJECT_MAPPER.readValue(bytes, KpiExposureConfig.class);
    }
}
