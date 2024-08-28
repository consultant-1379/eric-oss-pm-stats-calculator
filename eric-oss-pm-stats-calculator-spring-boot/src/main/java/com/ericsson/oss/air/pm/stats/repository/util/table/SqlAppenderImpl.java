/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.table;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.calculator.api.model.KpiDataType;
import com.ericsson.oss.air.pm.stats.repository.util.table.api.SqlAppender;
import com.ericsson.oss.air.pm.stats.repository.util.table.util.StringUtils;

import lombok.NonNull;

@ApplicationScoped
public class SqlAppenderImpl implements SqlAppender {
    private static final String AGGREGATION_BEGIN_TIME = "aggregation_begin_time";
    private static final String AGGREGATION_END_TIME = "aggregation_end_time";

    @Override
    public void appendPartition(final StringBuilder stringBuilder) {
        stringBuilder.append(String.format(" PARTITION BY RANGE ( %s ) WITH ( OIDS = FALSE );", AGGREGATION_BEGIN_TIME));
    }

    @Override
    public void appendTimestampColumns(final StringBuilder stringBuilder) {
        stringBuilder.append(String.format("%s %3$s NOT NULL, %s %3$s",
                AGGREGATION_BEGIN_TIME,
                AGGREGATION_END_TIME,
                KpiDataType.POSTGRES_TIMESTAMP.getPostgresType()));
    }

    @Override
    public void appendPrimaryKey(final StringBuilder stringBuilder, final Collection<String> columns) {
        stringBuilder.append(String.format("PRIMARY KEY (%s)", String.join(", ", StringUtils.enquoteCollectionElements(columns))));
    }

    @Override
    public void appendColumnNameAndType(final StringBuilder stringBuilder, @NonNull final String name, @NonNull final String objectType) {
        stringBuilder.append(String.format("%s %s, ", StringUtils.enquoteLiteral(name), KpiDataType.forValue(objectType).getPostgresType()));
    }

    @Override
    public void appendAggregationElementColumns(final StringBuilder stringBuilder,
                                                final Collection<String> aggregationElements,
                                                final Map<String, KpiDataType> validAggregationElements) {
        final String aggregationElementColumns =
                aggregationElements.stream()
                        .map(aggregationElement -> String.format("%s %s NOT NULL, ",
                                StringUtils.enquoteLiteral(aggregationElement),
                                validAggregationElements.get(aggregationElement).getPostgresType()))
                        .collect(Collectors.joining());

        stringBuilder.append(aggregationElementColumns);

    }
}
