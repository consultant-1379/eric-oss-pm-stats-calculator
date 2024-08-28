/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka;

import org.apache.kafka.common.TopicPartition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PartitionTest {
    @Test
    void shouldVerifyFrom() {
        final Partition topic = Partition.from(new TopicPartition("kafka-topic", 10));

        Assertions.assertThat(topic.number()).isEqualTo(10);
    }
}