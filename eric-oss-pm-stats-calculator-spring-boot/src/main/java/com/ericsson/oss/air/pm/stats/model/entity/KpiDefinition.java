/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.model.entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KpiDefinition {
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ALIAS = "alias";
    public static final String COLUMN_EXPRESSION = "expression";
    public static final String COLUMN_OBJECT_TYPE = "object_type";
    public static final String COLUMN_AGGREGATION_TYPE = "aggregation_type";
    public static final String COLUMN_AGGREGATION_PERIOD = "aggregation_period";
    public static final String COLUMN_AGGREGATION_ELEMENTS = "aggregation_elements";
    public static final String COLUMN_EXPORTABLE = "exportable";
    public static final String COLUMN_INP_DATA_IDENTIFIER = "inp_data_identifier";
    public static final String COLUMN_SCHEMA_DATA_SPACE = "schema_data_space";
    public static final String COLUMN_SCHEMA_CATEGORY = "schema_category";
    public static final String COLUMN_SCHEMA_NAME = "schema_name";
    public static final String COLUMN_EXECUTION_GROUP_ID = "execution_group_id";
    public static final String COLUMN_DATA_RELIABILITY_OFFSET = "data_reliability_offset";
    public static final String COLUMN_DATA_LOOKBACK_LIMIT = "data_lookback_limit";
    public static final String COLUMN_FILTERS = "filters";
    public static final String COLUMN_REEXPORT_LATE_DATA = "reexport_late_data";
    public static final String COLUMN_SCHEMA_DETAIL_ID = "schema_detail_id";
    public static final String COLUMN_SCHEMA_DETAIL = "schema_detail";
    public static final String COLUMN_TIME_DELETED = "time_deleted";
    public static final String COLLECTION_ID = "collection_id";
}
