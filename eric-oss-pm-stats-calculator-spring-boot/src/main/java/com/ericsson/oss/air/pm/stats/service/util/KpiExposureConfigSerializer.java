/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import com.ericsson.oss.air.pm.stats.calculator.api.dto.kafka.query.service.KpiExposureConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class KpiExposureConfigSerializer implements Serializer<KpiExposureConfig> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, KpiExposureConfig kpiExposureConfig) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(kpiExposureConfig);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing KpiExposureConfig to byte[]", e);
        }
    }
}
