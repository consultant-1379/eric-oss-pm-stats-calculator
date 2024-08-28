/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.scheduler.execution.order.compute;

import static lombok.AccessLevel.PUBLIC;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.air.pm.stats.calculation.limits.ReadinessBoundCalculator;
import com.ericsson.oss.air.pm.stats.graph.utils.GraphUtils;
import com.ericsson.oss.air.pm.stats.scheduler.execution.order.model.ComplexExecutionOrder;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor(access = PUBLIC)
public class ComplexExecutionOrderFilterer {
    @Inject
    private ReadinessBoundCalculator readinessBoundCalculator;

    public Queue<String> filterExecutionOrder(@NonNull final ComplexExecutionOrder complexExecutionOrder) {
        final Queue<String> filteringList = new ConcurrentLinkedQueue<>(complexExecutionOrder.getOrder());
        log.info("Order of all the complex group's: '{}'", filteringList);

        filteringList.stream()
                .filter(Predicate.not(readinessBoundCalculator::isReliablyCalculableGroup))
                .map(complexExecutionGroup -> GraphUtils.findAllParentsFromVertex(complexExecutionOrder.getGraph(), complexExecutionGroup))
                .forEach(filteringList::removeAll);

        return filteringList;
    }

}
