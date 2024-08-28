/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table.api;

import java.util.Collection;
import java.util.Map;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;

public interface SqlAppender {
    /**
     * Appends PARTITION statement.
     *
     * @param stringBuilder to append the PARTITION statement to.
     */
    void appendPartition(StringBuilder stringBuilder);

    /**
     * Appends the timestamp columns.
     *
     * @param stringBuilder to append the timestamp columns to.
     */
    void appendTimestampColumns(StringBuilder stringBuilder);

    /**
     * Appends the provided columns as primary key.
     *
     * @param stringBuilder to append the primary keys to.
     * @param columns       the columns to append.
     */
    void appendPrimaryKey(StringBuilder stringBuilder, Collection<String> columns);

    /**
     * Appends KPI Definition as a column.
     *
     * @param stringBuilder to append the KPI Definition to.
     * @param name          name of the KPI definition
     * @param objectType    object type of the KPI Definition
     */
    void appendColumnNameAndType(StringBuilder stringBuilder, String name, String objectType);

    /**
     * Appends aggregation elements as columns.
     *
     * @param stringBuilder            to append aggregation elements to.
     * @param aggregationElements      the {@link Collection} of aggregation element names to append.
     * @param validAggregationElements the {@link Map} of valid aggregation element names and types.
     */
    void appendAggregationElementColumns(StringBuilder stringBuilder, Collection<String> aggregationElements, Map<String, KpiDataType> validAggregationElements);
}
