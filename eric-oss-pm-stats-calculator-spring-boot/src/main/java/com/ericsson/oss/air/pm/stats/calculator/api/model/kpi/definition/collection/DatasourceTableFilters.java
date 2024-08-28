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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode
@NoArgsConstructor(staticName = "newInstance")
public class DatasourceTableFilters {
    @Delegate(types = MapDelegate.class)
    private Map<Datasource, Map<Table, List<Filter>>> map = new HashMap<>();

    private DatasourceTableFilters(final int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    public static DatasourceTableFilters newInstance(final int initialCapacity) {
        return new DatasourceTableFilters(initialCapacity);
    }

    public boolean isFilterable(final Datasource datasource, final Table table) {
        final Map<Table, List<Filter>> tableFilters = get(datasource);
        return Objects.nonNull(tableFilters) && tableFilters.containsKey(table);
    }

    public List<Filter> getFilters(final Datasource datasource, final Table table) {
        return isFilterable(datasource, table)
                ? map.get(datasource).get(table)
                : Collections.emptyList();
    }

    public interface MapDelegate {
        Map<Table, List<Filter>> get(Datasource key);

        Map<Table, List<Filter>> put(Datasource key, Map<Table, List<Filter>> value);
    }
}
