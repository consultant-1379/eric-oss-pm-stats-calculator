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

import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.pm.stats.calculator.api.model.SchemaAttribute;
import com.ericsson.oss.air.pm.stats.calculator.api.model.SchemaElement;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * POJO for KPI objects defined in KPI model.
 */
@AllArgsConstructor
@ToString
public final class Kpi implements SchemaElement {

    private final List<KpiAttribute> attributes;

    public Kpi() {
        attributes = Collections.emptyList();
    }

    @Override
    public List<SchemaAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }
}
