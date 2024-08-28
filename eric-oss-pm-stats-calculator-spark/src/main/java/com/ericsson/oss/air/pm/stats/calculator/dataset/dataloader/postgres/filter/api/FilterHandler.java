/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.api;

import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.FilterType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import org.springframework.plugin.core.Plugin;

public interface FilterHandler extends Plugin<FilterType> {
    String filterSql(Table table, String filter);
}
