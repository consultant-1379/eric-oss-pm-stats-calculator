/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.model.collection;

import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.DatasourceType.EXPRESSION_TAG_KPI_DB;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.DatasourceType.EXPRESSION_TAG_KPI_IN_MEMORY;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.DatasourceType.EXPRESSION_TAG_KPI_POST_AGGREGATION;
import static com.ericsson.oss.air.pm.stats.common.model.collection.Datasource.DatasourceType.EXPRESSION_TAG_TABULAR_PARAMETERS;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.RegExUtils;

@Data(staticConstructor = "of")
public final class Datasource {

    public enum DatasourceType {
        EXPRESSION_TAG_KPI_IN_MEMORY {
            @Override
            public Datasource asDataSource() {
                return of("kpi_inmemory");
            }
        },
        EXPRESSION_TAG_KPI_POST_AGGREGATION {
            @Override
            public Datasource asDataSource() {
                return of("kpi_post_agg");
            }
        },
        EXPRESSION_TAG_KPI_DB {
            @Override
            public Datasource asDataSource() {
                return of("kpi_db");
            }
        },
        EXPRESSION_TAG_TABULAR_PARAMETERS {
            @Override
            public Datasource asDataSource() {
                return of("tabular_parameters");
            }
        };

        public abstract Datasource asDataSource();
    }

    public static final Datasource KPI_IN_MEMORY = EXPRESSION_TAG_KPI_IN_MEMORY.asDataSource();
    public static final Datasource KPI_POST_AGGREGATION = EXPRESSION_TAG_KPI_POST_AGGREGATION.asDataSource();
    public static final Datasource KPI_DB = EXPRESSION_TAG_KPI_DB.asDataSource();
    public static final Datasource TABULAR_PARAMETERS = EXPRESSION_TAG_TABULAR_PARAMETERS.asDataSource();

    @NonNull
    private final String name;

    public static Datasource datasource(final String name) {
        return new Datasource(name);
    }

    public Database toDatabase() {
        return new Database(RegExUtils.removeAll(name, "_"));
    }

    public static boolean isInMemory(final Datasource datasource) {
        return KPI_IN_MEMORY.equals(datasource);
    }

    public static boolean isKpiDb(final Datasource datasource) {
        return KPI_DB.equals(datasource);
    }

    public static List<Datasource> inMemoryDatasources() {
        return Arrays.stream(DatasourceType.values())
                .map(DatasourceType::asDataSource)
                .filter(datasource -> datasource.isInMemory())
                .collect(toList());
    }

    public boolean isNonInMemory() {
        return !isInMemory();
    }

    public boolean isInMemory() {
        return equals(KPI_IN_MEMORY) || equals(KPI_POST_AGGREGATION);
    }

    public boolean isUnknown() {
        return !isInternal();
    }

    public boolean isInternal() {
        return isInMemory() || equals(KPI_DB) || equals(TABULAR_PARAMETERS);
    }
}