/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.sqlparser.extractor;

import java.util.function.Consumer;
import javax.enterprise.context.ApplicationScoped;

import com.ericsson.oss.air.pm.stats.common.sqlparser.util.SparkTrees;

import lombok.NonNull;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;

@ApplicationScoped
public class LogicalPlanExtractor {
    public <T> void consumeType(final LogicalPlan logicalPlan, @NonNull final Class<? extends T> type, final Consumer<T> consumer) {
        SparkTrees.asJavaStreamWithChildren(logicalPlan)
                .filter(plan -> type.isAssignableFrom(plan.getClass()))
                .map(type::cast)
                .forEach(consumer);
    }
}
