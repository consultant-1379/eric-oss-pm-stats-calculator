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

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.apache.kafka.common.TopicPartition;

/**
 * Class representing a <strong>Kafka</strong> partition.
 */
@Accessors(fluent = true)
@Data(staticConstructor = "of")
public final class Partition {
    private final int number;

    public static Partition from(@NonNull final TopicPartition topicPartition) {
        return of(topicPartition.partition());
    }
}
