/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi;

import com.ericsson.oss.air.pm.stats.calculator.api.model.SchemaAttribute;
import com.ericsson.oss.air.pm.stats.calculator.api.model.SqlDataType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for attributes defined in the KPI model.
 */
@NoArgsConstructor
@Data
public class KpiAttribute implements SchemaAttribute {

    private String name;
    private SqlDataType type;
    private boolean optional;
    private boolean parameterizable;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SqlDataType getType() {
        return type;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public boolean isParameterizable() {
        return parameterizable;
    }
}
