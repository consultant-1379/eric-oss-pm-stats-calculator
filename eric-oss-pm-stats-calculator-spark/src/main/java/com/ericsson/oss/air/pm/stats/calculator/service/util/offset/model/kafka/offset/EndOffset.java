/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.service.util.offset.model.kafka.offset;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public final class EndOffset implements Comparable<EndOffset> {
    @Getter(AccessLevel.NONE) private final Offset offset;

    private EndOffset(final long offset) {
        this.offset = Offset.of(offset);
    }

    public static EndOffset of(final long endOffset) {
        return new EndOffset(endOffset);
    }

    public long number() {
        return offset.number();
    }

    @Override
    public int compareTo(@NonNull final EndOffset o) {
        return offset.compareTo(o.offset);
    }

}
