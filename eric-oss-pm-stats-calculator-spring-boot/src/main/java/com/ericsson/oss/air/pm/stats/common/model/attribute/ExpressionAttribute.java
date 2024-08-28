/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.attribute;


public interface ExpressionAttribute extends Attribute<String> {
    default boolean containsPostAggregationDatasource() {
        final String expression = value();
        return expression.contains("kpi_post_agg://");
    }
}
