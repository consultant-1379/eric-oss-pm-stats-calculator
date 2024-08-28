/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestProcessedOffset;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public final class StartOffset implements Comparable<StartOffset> {
    @Getter(AccessLevel.NONE) private final Offset offset;

    private StartOffset(final long offset) {
        this.offset = Offset.of(offset);
    }

    public static StartOffset of(final long startOffset) {
        return new StartOffset(startOffset);
    }

    public static StartOffset from(@NonNull final LatestProcessedOffset latestProcessedOffset) {
        return new StartOffset(latestProcessedOffset.getTopicPartitionOffset());
    }

    public long number() {
        return offset.number();
    }

    @Override
    public int compareTo(@NonNull final StartOffset o) {
        return offset.compareTo(o.offset);
    }
}
