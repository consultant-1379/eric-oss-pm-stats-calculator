/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.sql;

import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DAILY_AGGREGATION_PERIOD_IN_MINUTES;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.DEFAULT_AGGREGATION_PERIOD_INT;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.FIFTEEN_MINUTES_AGGREGATION_PERIOD;
import static com.ericsson.oss.air.pm.stats.common.model.constants.KpiCalculatorConstants.HOURLY_AGGREGATION_PERIOD_IN_MINUTES;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.date_format;

import com.ericsson.oss.air.pm.stats.common.spark.udf.internal.TruncateToFifteenMinute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.types.DataTypes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlDateUtils {

    public static String truncateToAggregationPeriod(@NonNull final String column, final int aggregationPeriod) {
        switch (aggregationPeriod) {
            case DEFAULT_AGGREGATION_PERIOD_INT:
                return dateFormat(col(column), "'yyyy-MM-dd'");
            case FIFTEEN_MINUTES_AGGREGATION_PERIOD:
                return String.format("%s(%s)", TruncateToFifteenMinute.TRUNCATE_TO_FIFTEEN_MINUTES, column);
            case HOURLY_AGGREGATION_PERIOD_IN_MINUTES:
                return dateFormat(col(column), "'yyyy-MM-dd HH'");
            case DAILY_AGGREGATION_PERIOD_IN_MINUTES:
                /* Identical branch to keep cases in ascending order */
                return dateFormat(col(column), "'yyyy-MM-dd'");
            default:
                throw new IllegalArgumentException(String.format("Aggregation period '%d' is not supported", aggregationPeriod));
        }
    }

    private static String dateFormat(final Column col, final String format) {
        return date_format(col, format).cast(DataTypes.TimestampType).toString();
    }

}
