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

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.api.FilterHandler;
import com.ericsson.oss.air.pm.stats.calculator.sql.SqlFilterCreator;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomFilterHandlerImpl implements FilterHandler {

    @Override
    public boolean supports(@NonNull final FilterType delimiter) {
        return delimiter == FilterType.CUSTOM;
    }

    @Override
    public String filterSql(@NonNull final Table table, final String filter) {
        final SqlFilterCreator sqlFilterCreator = new SqlFilterCreator();
        sqlFilterCreator.selectAll(table.getName()).where().addFilter(filter);
        return sqlFilterCreator.build();
    }
}
