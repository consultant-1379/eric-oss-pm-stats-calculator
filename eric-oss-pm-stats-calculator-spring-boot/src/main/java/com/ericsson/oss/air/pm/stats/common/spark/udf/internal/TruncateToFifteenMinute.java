/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udf.internal;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nullable;

import org.apache.spark.sql.api.java.UDF1;

public class TruncateToFifteenMinute implements UDF1<Timestamp, Timestamp> {
    private static final long serialVersionUID = 1L;

    public static final String TRUNCATE_TO_FIFTEEN_MINUTES = "TRUNCATE_TO_FIFTEEN_MINUTE";

    @Override
    @Nullable
    public Timestamp call(final Timestamp timestamp) throws Exception {
        if (timestamp == null) {
            return null;
        }

        final int period = 15;
        final LocalDateTime localDateTime = timestamp.toLocalDateTime();
        final int floor = period * (localDateTime.getMinute() / period);

        return Timestamp.valueOf(localDateTime.truncatedTo(ChronoUnit.HOURS).plusMinutes(floor));
    }

}
