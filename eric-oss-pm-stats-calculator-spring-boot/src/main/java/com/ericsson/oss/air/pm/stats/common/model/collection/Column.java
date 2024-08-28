/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.collection;

import java.util.Collection;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;

import lombok.Data;

@Data(staticConstructor = "of")
public class Column {
    /**
     * <strong>aggregation_begin_time</strong> column must be contained by a {@link DatasourceType#FACT} table.
     */
    public static final Column AGGREGATION_BEGIN_TIME = of("aggregation_begin_time");
    /**
     * <strong>aggregation_end_time</strong> column must be contained by a {@link DatasourceType#FACT} table.
     */
    public static final Column AGGREGATION_END_TIME = of("aggregation_end_time");

    private static final Set<Column> INTERNALS = Set.of(AGGREGATION_BEGIN_TIME, AGGREGATION_END_TIME);

    private final String name;

    public static Collection<Column> internals() {
        return INTERNALS;
    }

    public boolean isInternal() {
        return internals().contains(this);
    }
}
