/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.datasource;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import lombok.SneakyThrows;
import org.apache.commons.lang3.concurrent.locks.LockingVisitors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DatasourceRegistryTest {
    static final Datasource DATASOURCE = Datasource.of("datasource");

    /**
     * <strong>Singleton</strong>
     */
    final DatasourceRegistry objectUnderTest = DatasourceRegistry.getInstance();

    @BeforeEach
    void setUp() {
        resetSingleton();
    }

    @AfterEach
    void tearDown() {
        resetSingleton();
    }

    @Test
    void shouldReturnUnmodifiableDatasourceRegistry() {
        Assertions.assertThat(objectUnderTest.getAllDatasourceRegistry().entrySet()).isUnmodifiable();
    }

    @ParameterizedTest
    @MethodSource("provideContainsDataSourceData")
    void shouldValidateContainsDataSource(final List<Datasource> dataSources, final Datasource toFind, final boolean expected) {
        dataSources.forEach(datasource -> objectUnderTest.addDatasource(datasource, new JdbcDatasource("connection", new Properties())));
        final boolean actual = objectUnderTest.containsDatasource(toFind);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldAdd_andThenGetDatasource() {
        final JdbcDatasource connection = new JdbcDatasource("connection", new Properties());

        objectUnderTest.addDatasource(DATASOURCE, connection);

        final JdbcDatasource actual = objectUnderTest.getJdbcDatasource(DATASOURCE);
        Assertions.assertThat(actual).isEqualTo(connection);
    }

    @Test
    void shouldAdd_andThenGetDatasource_byDatasource() {
        final JdbcDatasource connection = new JdbcDatasource("connection", new Properties());

        objectUnderTest.addDatasource(DATASOURCE, connection);

        final JdbcDatasource actual = objectUnderTest.getJdbcDatasource(DATASOURCE);
        Assertions.assertThat(actual).isEqualTo(connection);
    }

    @Test
    void shouldGetDataSources() {
        objectUnderTest.addDatasource(Datasource.of("datasource1"), new JdbcDatasource("connection1", new Properties()));
        objectUnderTest.addDatasource(Datasource.of("datasource2"), new JdbcDatasource("connection2", new Properties()));

        final Set<Datasource> actual = objectUnderTest.getDataSources();

        Assertions.assertThat(actual).containsExactlyInAnyOrder(
                Datasource.of("datasource1"),
                Datasource.of("datasource2")
        );
    }

    @MethodSource("provideDatasourceHasTypeData")
    @ParameterizedTest(name = "[{index}] Datasource with name: ''{0}'' has type of: ''{1}'' ==> ''{2}''")
    void shouldDatasourceHasType(final Datasource datasource, final String type, final Optional<Boolean> expected) {
        final Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty("type", "DIM");

        final JdbcDatasource connection = new JdbcDatasource("connection", jdbcProperties);
        objectUnderTest.addDatasource(DATASOURCE, connection);

        Assertions.assertThat(objectUnderTest.datasourceHasType(datasource, type)).isEqualTo(expected);
    }

    @Nested
    @DisplayName("Test Datasource type")
    @TestInstance(Lifecycle.PER_CLASS)
    class DatasourceTypeTest {

        @BeforeEach
        void setUp() {
            final Properties jdbcProperties = new Properties();
            jdbcProperties.setProperty("type", "DIM");
            objectUnderTest.addDatasource(Datasource.of("dimDatasource"), new JdbcDatasource("connection1", jdbcProperties));

            final Properties jdbcProperties1 = new Properties();
            jdbcProperties1.setProperty("type", "FACT");
            objectUnderTest.addDatasource(Datasource.of("factDatasource"), new JdbcDatasource("connection1", jdbcProperties1));

        }

        @MethodSource("provideIsDim")
        @ParameterizedTest(name = "[{index}] Datasource with name: ''{0}'' has type DIM, if datasource missing then default is: ''{1}'' ==> ''{2}''")
        void verifyIsDim(final Datasource datasource, final boolean defaultIfMissing, final boolean expected) {
            Assertions.assertThat(objectUnderTest.isDim(datasource, defaultIfMissing)).isEqualTo(expected);
        }

        @MethodSource("provideIsFact")
        @ParameterizedTest(name = "[{index}] Datasource with name: ''{0}'' has type Fact, if datasource missing then default is: ''{1}'' ==> ''{2}''")
        void verifyIsFact(final Datasource datasource, final boolean defaultIfMissing, final boolean expected) {
            Assertions.assertThat(objectUnderTest.isFact(datasource, defaultIfMissing)).isEqualTo(expected);
        }

        Stream<Arguments> provideIsDim() {
            return Stream.of(Arguments.of(Datasource.of("unknownDatasource"), false, false),
                    Arguments.of(Datasource.of("unknownDatasource"), true, true),
                    Arguments.of(Datasource.of("dimDatasource"), false, true),
                    Arguments.of(Datasource.of("dimDatasource"), true, true),
                    Arguments.of(Datasource.of("factDatasource"), false, false),
                    Arguments.of(Datasource.of("factDatasource"), true, false));
        }

        Stream<Arguments> provideIsFact() {
            return Stream.of(Arguments.of(Datasource.of("unknownDatasource"), false, false),
                    Arguments.of(Datasource.of("unknownDatasource"), true, true),
                    Arguments.of(Datasource.of("dimDatasource"), true, false),
                    Arguments.of(Datasource.of("dimDatasource"), false, false),
                    Arguments.of(Datasource.of("factDatasource"), false, true),
                    Arguments.of(Datasource.of("factDatasource"), true, true));
        }
    }

    @Nested
    @DisplayName("Test DIM table")
    @TestInstance(Lifecycle.PER_CLASS)
    class DimTable {

        @MethodSource("provideDimTableData")
        @ParameterizedTest(name = "[{index}] Datasource with name: ''{0}'' and table: ''{1}'' is DIM ==> ''{2}''")
        void verifyIsDimTable(final Datasource datasource, final Table table, final boolean expected) {
            final Properties jdbcProperties = new Properties();
            jdbcProperties.setProperty("type", "DIM");

            final JdbcDatasource connection = new JdbcDatasource("connection", jdbcProperties);
            objectUnderTest.addDatasource(DATASOURCE, connection);

            final boolean actual = objectUnderTest.isDimTable(datasource, table);

            Assertions.assertThat(actual).isEqualTo(expected);
        }

        Stream<Arguments> provideDimTableData() {
            return Stream.of(
                    Arguments.of(Datasource.of("unknownDatasource"), Table.of("kpi_sector"), false),
                    Arguments.of(Datasource.of("kpi_db"), Table.of("kpi_sector"), true),
                    Arguments.of(DATASOURCE, Table.of("kpi_sector"), true)
            );
        }
    }

    static Stream<Arguments> provideContainsDataSourceData() {
        final Datasource red = Datasource.of("red");
        final Datasource blue = Datasource.of("blue");
        final Datasource green = Datasource.of("green");

        return Stream.of(
                Arguments.of(List.of(red, blue, green), green, true),
                Arguments.of(List.of(red, blue), green, false),
                Arguments.of(List.of(), blue, false)
        );
    }

    private static Stream<Arguments> provideDatasourceHasTypeData() {
        return Stream.of(Arguments.of(Datasource.of("unknownDatasource"), "unknownType", Optional.empty()),
                Arguments.of(DATASOURCE, "unknownType", Optional.of(false)),
                Arguments.of(DATASOURCE, "dIm", Optional.of(true)),
                Arguments.of(DATASOURCE, "DIM", Optional.of(true)));
    }

    @SneakyThrows
    private void resetSingleton() {
        final Field field = DatasourceRegistry.class.getDeclaredField("lock");
        field.setAccessible(true);
        field.set(objectUnderTest, LockingVisitors.reentrantReadWriteLockVisitor(new ConcurrentHashMap<>(0)));
    }
}