/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode
@NoArgsConstructor(staticName = "newInstance")
public class DatasourceFilters {
    @Delegate(types = MapDelegate.class)
    private final Map<Datasource, List<Filter>> map = new HashMap<>();

    public interface MapDelegate {
        int size();

        Set<Entry<Datasource, List<Filter>>> entrySet();

        List<Filter> computeIfAbsent(Datasource key, Function<? super Datasource, ? extends List<Filter>> mappingFunction);
    }
}
