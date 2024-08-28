/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.api.FilterHandlerRegistryFacade;
import com.ericsson.oss.air.pm.stats.calculator.sql.SqlFilterCreator;
import com.ericsson.oss.air.pm.stats.calculator.util.KpiCalculatorTimeSlot;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterPreparatoryImpl {
    private final FilterHandlerRegistryFacade filterHandlerRegistryFacade;

    public Filter generateTimeBasedFilter(@NonNull final KpiCalculatorTimeSlot kpiCalculatorTimeSlot) {
        return Filter.of(
                SqlFilterCreator.create()
                                .aggregationBeginTimeBetween(kpiCalculatorTimeSlot.getStartTimestamp(), kpiCalculatorTimeSlot.getEndTimestamp())
                                .build()
        );
    }

    public String prepareFilter(final FilterType filterType, final Table table, final String filter) {
        return StringUtils.isEmpty(filter)
                ? SqlFilterCreator.create().selectAll(table.getName()).build()
                : filterHandlerRegistryFacade.filterHandler(filterType).filterSql(table, filter);
    }
}
