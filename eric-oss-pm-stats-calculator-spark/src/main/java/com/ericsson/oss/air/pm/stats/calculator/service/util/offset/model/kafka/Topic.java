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

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.MessageCounter;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.apache.kafka.common.TopicPartition;

/**
 * Class representing a <strong>Kafka</strong> topic.
 * <br>
 * <strong>IMPORTANT</strong>: {@link Topic#name} is used as cache key in {@link MessageCounter#countMessagesByPartition(Topic)}
 */
@Accessors(fluent = true)
@Data(staticConstructor = "of")
public final class Topic {
    private final String name;

    public static Topic from(@NonNull final TopicPartition topicPartition) {
        return of(topicPartition.topic());
    }
}
