/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.data.datasource;


import com.ericsson.oss.air.pm.stats.common.model.DatasourceType;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.concurrent.locks.LockingVisitors.ReadWriteLockVisitor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.ericsson.oss.air.pm.stats.common.model.constants.DatasourceConstants.PROPERTY_TYPE;
import static org.apache.commons.lang3.concurrent.locks.LockingVisitors.reentrantReadWriteLockVisitor;

/**
 * Stores a map of datasource names to {@link JdbcDatasource}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatasourceRegistry {
    private static final DatasourceRegistry INSTANCE = new DatasourceRegistry();

    private final ReadWriteLockVisitor<ConcurrentHashMap<Datasource, JdbcDatasource>> lock = reentrantReadWriteLockVisitor(new ConcurrentHashMap<>());

    public static DatasourceRegistry getInstance() {
        return INSTANCE;
    }

    public boolean containsDatasource(final Datasource datasource) {
        return lock.applyReadLocked(registry -> registry.containsKey(datasource));
    }

    public Map<Datasource, JdbcDatasource> getAllDatasourceRegistry() {
        return lock.applyReadLocked(Collections::unmodifiableMap);
    }

    public Set<Datasource> getDataSources() {
        return lock.applyReadLocked(ConcurrentHashMap::keySet);
    }

    public void addDatasource(@NonNull final Datasource datasource, final JdbcDatasource jdbcDatasource) {
        lock.acceptWriteLocked(registry -> registry.put(datasource, jdbcDatasource));
    }

    public JdbcDatasource getJdbcDatasource(@NonNull final Datasource datasource) {
        return lock.applyReadLocked(registry -> registry.get(datasource));

    }

    public boolean isDimTable(final Datasource datasource, final Table table) {
        return (Datasource.isKpiDb(datasource) && table.doesNotContainNumber())
                || datasourceHasType(datasource, DatasourceType.DIM.toString()).orElse(false);
    }

    /**
     * This method checks the type of the datasource.
     *
     * @param datasource     datasource to be checked
     * @param datasourceType datasource type to be checked with
     * @return the comparison result wrapped in an {@link Optional}
     */
    public Optional<Boolean> datasourceHasType(@NonNull final Datasource datasource, final String datasourceType) {
        return lock.applyReadLocked(registry ->
                Optional.ofNullable(registry.get(datasource))
                        .map(JdbcDatasource::getJdbcProperties)
                        .map(DatasourceRegistry::getPropertyType)
                        .map(propertyType -> propertyType.equalsIgnoreCase(datasourceType))
        );
    }

    public boolean isDim(final Datasource datasource, final boolean defaultIfMissing) {
        return datasourceHasType(datasource, DatasourceType.DIM.toString()).orElse(defaultIfMissing);
    }

    public boolean isFact(final Datasource datasource, final boolean defaultIfMissing) {
        return datasourceHasType(datasource, DatasourceType.FACT.toString()).orElse(defaultIfMissing);
    }

    private static String getPropertyType(@NonNull final Properties properties) {
        return properties.getProperty(PROPERTY_TYPE);
    }
}
