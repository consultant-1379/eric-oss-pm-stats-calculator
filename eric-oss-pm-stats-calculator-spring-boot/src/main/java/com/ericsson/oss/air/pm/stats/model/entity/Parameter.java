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

import kpi.model.ondemand.ParameterType;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Builder(setterPrefix = "with")
public class Parameter {
    public static final String COLUMN_PARAMETERS_ID = "id";
    public static final String COLUMN_PARAMETERS_NAME = "name";
    public static final String COLUMN_PARAMETERS_TYPE = "type";
    public static final String COLUMN_PARAMETERS_TABULAR_PARAMETER_ID = "tabular_parameter_id";
    private final Integer id;
    private final String name;
    private final ParameterType type;
    private final TabularParameter tabularParameter;
}
