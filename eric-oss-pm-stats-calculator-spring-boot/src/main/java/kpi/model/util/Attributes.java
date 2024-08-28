/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package kpi.model.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Attributes {
    public static final String ATTRIBUTE_AGGREGATION_ELEMENTS = "aggregation_elements";
    public static final String ATTRIBUTE_AGGREGATION_PERIOD = "aggregation_period";
    public static final String ATTRIBUTE_AGGREGATION_TYPE = "aggregation_type";
    public static final String ATTRIBUTE_ALIAS = "alias";
    public static final String ATTRIBUTE_DATA_LOOK_BACK_LIMIT = "data_lookback_limit";
    public static final String ATTRIBUTE_DATA_RELIABILITY_OFFSET = "data_reliability_offset";
    public static final String ATTRIBUTE_EXECUTION_GROUP = "execution_group";
    public static final String ATTRIBUTE_EXPRESSION = "expression";
    public static final String ATTRIBUTE_FILTERS = "filters";
    public static final String ATTRIBUTE_INP_DATA_IDENTIFIER = "inp_data_identifier";
    public static final String ATTRIBUTE_IS_EXPORTABLE = "exportable";
    public static final String ATTRIBUTE_KPI_DEFINITIONS = "kpi_definitions";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_OBJECT_TYPE = "object_type";
    public static final String ATTRIBUTE_REEXPORT_LATE_DATA = "reexport_late_data";

    public static final String AGGREGATION_ELEMENT = "aggregation_element";
    public static final String FILTER_ELEMENT = "filter";

    public static final String ATTRIBUTE_RETENTION_PERIOD = "retention_period_in_days";
}
