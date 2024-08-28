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

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Class representing a <strong>Kafka</strong> partition offset.
 */
@Accessors(fluent = true)
@Data(staticConstructor = "of")
public final class Offset implements Comparable<Offset> {
    private final long number;

    @Override
    public int compareTo(@NonNull final Offset o) {
        return Long.compare(number, o.number);
    }
}
