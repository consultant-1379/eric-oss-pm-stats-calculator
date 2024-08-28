/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.constants;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants that are used on KPI calculation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KpiCalculatorConstants {

    public static final int FIFTEEN_MINUTES_AGGREGATION_PERIOD = 15;
    public static final int HOURLY_AGGREGATION_PERIOD_IN_MINUTES = 60;
    public static final int DAILY_AGGREGATION_PERIOD_IN_MINUTES = 1_440;
    public static final int DEFAULT_AGGREGATION_PERIOD_INT = -1;
    public static final String DEFAULT_AGGREGATION_PERIOD = String.valueOf(DEFAULT_AGGREGATION_PERIOD_INT);

    // Database constants
    public static final String AGGREGATION_END_TIME_COLUMN = "aggregation_end_time";
    public static final String AGGREGATION_BEGIN_TIME_COLUMN = "aggregation_begin_time";
    public static final String CALCULATION_ID = "calculation_id";
    public static final String EXECUTION_GROUP = "execution_group";

    public static final String LATEST_SOURCE_DATA_PRIMARY_KEY = "source,aggregation_period_minutes,execution_group";

    public static final String LATEST_SOURCE_DATA = "latest_source_data";

    public static final String KPI_DEFINITION_TABLE_NAME = "kpi_definition";

    public static final String CELL_GUID = "cell_guid";
    public static final String CELL_SECTOR = "cell_sector";
    public static final String SECTOR = "sector";
    public static final String CELL_GUID_SIMPLE = "cell_guid_simple";
    public static final String RELATION_GUID_SOURCE_GUID_TARGET_GUID = "relation_guid_source_guid_target_guid";

    public static final String COMMA_AND_SPACE_SEPARATOR = ", ";

    public static final String PERCENTILE_INDEX_80_UDAF = "PERCENTILE_INDEX_80";
    public static final String PERCENTILE_INDEX_90_UDAF = "PERCENTILE_INDEX_90";
    public static final String MAX_UNION_AGGREGATION_TYPE = "max";

    public static final String SQL_WHERE = "WHERE";
    public static final String SQL_FROM = "FROM";

    public static final String EXECUTION_GROUP_ON_DEMAND_CALCULATION = "ON_DEMAND";
    public static final UUID DEFAULT_COLLECTION_ID = UUID.fromString("29dc1bbf-7cdf-421b-8fc9-e363889ada79");
    /**
     * For metrics.
     */
    public static final String SPARK_ERICSSON_JOB_DESCRIPTION = "spark.ericsson.job.description";
    /**
     * For debugging purposes in {@code Spark history}.
     */
    public static final String SPARK_JOB_DESCRIPTION = "spark.job.description";
}
