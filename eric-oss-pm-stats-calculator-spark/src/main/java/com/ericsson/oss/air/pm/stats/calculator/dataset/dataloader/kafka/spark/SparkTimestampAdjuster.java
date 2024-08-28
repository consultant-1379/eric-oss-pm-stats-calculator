/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.spark;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.from_unixtime;
import static org.apache.spark.sql.functions.lit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema.Field;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.functions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SparkTimestampAdjuster {

    /**
     * The Timestamp values on the <strong>Kafka</strong> topics are in milliseconds.
     * <br>
     * Internally <strong>PM Stats Calculator</strong> works with Timestamps,
     * but the <strong>Spark</strong> provided out-of-box converter {@link functions#from_unixtime(Column)} function handles seconds only,
     * so it needs to be divided by <strong>1_000</strong> to match with milliseconds.
     */
    public Column adjustTimestampColumn(@NonNull final Field field) {
        final String columnName = field.name();
        return functions.when(col(columnName).isNull(), col(columnName))
                        .otherwise(from_unixtime(col(columnName).divide(lit(1_000)))
        );
    }

    /**
     * The ropBeginTime and ropEndTime values on the <strong>Kafka</strong> topics are in String.
     * <br>
     * Internally <strong>PM Stats Calculator</strong> works with Timestamps,
     * so it needs to be converted to Timestamps.
     */
    public Column adjustStringToTimestampColumn(@NonNull final String fieldName) {
        return functions.to_timestamp(col(fieldName));
    }
}
