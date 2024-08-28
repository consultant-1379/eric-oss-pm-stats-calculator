/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.collection.DatasourceTables;
import com.ericsson.oss.air.pm.stats.calculator.repository.internal.api.DataSourceRepository;
import com.ericsson.oss.air.pm.stats.calculator.service.util.KpiDefinitionHelperImpl;
import com.ericsson.oss.air.pm.stats.common.model.collection.Datasource;
import com.ericsson.oss.air.pm.stats.common.model.collection.Table;
import com.ericsson.oss.air.pm.stats.data.datasource.DatasourceRegistry;
import com.ericsson.oss.air.pm.stats.data.datasource.JdbcDatasource;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SourceDataAvailabilityTest {
    static final Datasource DATASOURCE_1 = Datasource.of("datasource1");
    static final Datasource DATASOURCE_2 = Datasource.of("datasource2");

    static final JdbcDatasource JDBC_DATASOURCE_DUMMY = new JdbcDatasource("connection", new Properties());

    final List<KpiDefinition> kpiDefinitions = emptyList();

    @Mock DatasourceRegistry datasourceRegistryMock;
    @Mock DataSourceRepository dataSourceRepositoryMock;
    @Mock KpiDefinitionHelperImpl kpiDefinitionHelperMock;

    @InjectMocks SourceDataAvailability objectUnderTests;

    @Nested
    @DisplayName("get unavailable data sources")
    class GetUnavailableDataSources {
        @Test
        void shouldReturnUnavailableDataSources() {
            when(kpiDefinitionHelperMock.extractNonInMemoryDataSources(kpiDefinitions)).thenReturn(Sets.newLinkedHashSet(DATASOURCE_1, DATASOURCE_2));
            when(datasourceRegistryMock.containsDatasource(DATASOURCE_1)).thenReturn(true);
            when(datasourceRegistryMock.containsDatasource(DATASOURCE_2)).thenReturn(true);
            when(dataSourceRepositoryMock.isAvailable(DATASOURCE_1)).thenReturn(true);
            when(dataSourceRepositoryMock.isAvailable(DATASOURCE_2)).thenReturn(false);

            final Set<Datasource> actual = objectUnderTests.getUnavailableDataSources(kpiDefinitions);

            verify(kpiDefinitionHelperMock).extractNonInMemoryDataSources(kpiDefinitions);
            verify(datasourceRegistryMock).containsDatasource(DATASOURCE_1);
            verify(datasourceRegistryMock).containsDatasource(DATASOURCE_2);
            verify(dataSourceRepositoryMock).isAvailable(DATASOURCE_1);
            verify(dataSourceRepositoryMock).isAvailable(DATASOURCE_2);

            Assertions.assertThat(actual).containsExactlyInAnyOrder(DATASOURCE_2);
        }

        @Test
        void shouldReturnEmpty_whenRegistryDoesNotContainDataSource() {
            when(kpiDefinitionHelperMock.extractNonInMemoryDataSources(kpiDefinitions)).thenReturn(singleton(DATASOURCE_1));
            when(datasourceRegistryMock.containsDatasource(DATASOURCE_1)).thenReturn(false);

            final Set<Datasource> actual = objectUnderTests.getUnavailableDataSources(kpiDefinitions);

            verify(kpiDefinitionHelperMock).extractNonInMemoryDataSources(kpiDefinitions);
            verify(datasourceRegistryMock).containsDatasource(DATASOURCE_1);
            verifyNoMoreInteractions(dataSourceRepositoryMock);

            Assertions.assertThat(actual).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenDataSourceIsAvailable() {
            when(kpiDefinitionHelperMock.extractNonInMemoryDataSources(kpiDefinitions)).thenReturn(singleton(DATASOURCE_2));
            when(datasourceRegistryMock.containsDatasource(DATASOURCE_2)).thenReturn(true);
            when(dataSourceRepositoryMock.isAvailable(DATASOURCE_2)).thenReturn(true);

            when(dataSourceRepositoryMock.isAvailable(DATASOURCE_2)).thenReturn(true);

            final Set<Datasource> actual = objectUnderTests.getUnavailableDataSources(kpiDefinitions);

            verify(kpiDefinitionHelperMock).extractNonInMemoryDataSources(kpiDefinitions);
            verify(datasourceRegistryMock).containsDatasource(DATASOURCE_2);
            verify(dataSourceRepositoryMock).isAvailable(DATASOURCE_2);

            Assertions.assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("is data present for any data source")
    class IsDataPresentForAnyDataSource {
        final Timestamp testStart = Timestamp.valueOf(LocalDateTime.of(LocalDate.of(2_022, Month.JULY, 14), LocalTime.MIDNIGHT));
        final Timestamp testEnd = Timestamp.valueOf(LocalDateTime.of(LocalDate.of(2_022, Month.JULY, 15), LocalTime.MIDNIGHT));

        @Test
        void shouldReturnFalse_whenTheProvidedSourcesAreEmpty() {
            final boolean actual = objectUnderTests.isDataPresentForAnyDatasource(testStart, testEnd, DatasourceTables.newInstance(), emptyList());

            verifyNoMoreInteractions(dataSourceRepositoryMock);
            verifyNoMoreInteractions(datasourceRegistryMock);

            Assertions.assertThat(actual).isFalse();
        }

        @Test
        void shouldReturnFalse_whenTheProvidedSourcesAreTypeOfDIM() {
            final DatasourceTables datasourceTables = DatasourceTables.newInstance();
            datasourceTables.put(DATASOURCE_1, emptySet());

            when(datasourceRegistryMock.isFact(DATASOURCE_1, true)).thenReturn(false);
            final boolean actual = objectUnderTests.isDataPresentForAnyDatasource(testStart, testEnd, datasourceTables, emptyList());

            verify(datasourceRegistryMock).isFact(DATASOURCE_1, true);
            verifyNoMoreInteractions(dataSourceRepositoryMock);

            Assertions.assertThat(actual).isFalse();
        }

        @Test
        void shouldReturnFalse_whenDatasourceIsUnavailable() {
            final DatasourceTables datasourceTables = DatasourceTables.newInstance();
            datasourceTables.put(DATASOURCE_1, emptySet());

            when(kpiDefinitionHelperMock.extractNonInMemoryDataSources(kpiDefinitions)).thenReturn(singleton(DATASOURCE_1));
            when(datasourceRegistryMock.isFact(DATASOURCE_1, true)).thenReturn(true);
            when(datasourceRegistryMock.containsDatasource(DATASOURCE_1)).thenReturn(true);
            when(dataSourceRepositoryMock.isAvailable(DATASOURCE_1)).thenReturn(false);

            final boolean actual = objectUnderTests.isDataPresentForAnyDatasource(
                    testStart,
                    testEnd,
                    datasourceTables,
                    kpiDefinitions
            );

            verify(kpiDefinitionHelperMock).extractNonInMemoryDataSources(kpiDefinitions);
            verify(datasourceRegistryMock).isFact(DATASOURCE_1, true);
            verify(datasourceRegistryMock).containsDatasource(DATASOURCE_1);
            verify(dataSourceRepositoryMock).isAvailable(DATASOURCE_1);

            Assertions.assertThat(actual).isFalse();
        }

        @Nested
        @DisplayName("verify isDataPresentForAnyDatasource")
        class DataSourceContainsData {

            @ArgumentsSource(BlankStringsArgumentsProvider.class)
            @ParameterizedTest(name = "[{index}] Does contain data: ''{0}'' returns ==> ''{1}''")
            void verifyIsDataPresentForAnyDatasource(final boolean doesContainData, final boolean expected) {
                final DatasourceTables datasourceTables = DatasourceTables.newInstance();
                datasourceTables.put(DATASOURCE_1, Sets.newLinkedHashSet(Table.of("table1")));

                when(kpiDefinitionHelperMock.extractNonInMemoryDataSources(kpiDefinitions)).thenReturn(singleton(DATASOURCE_1));
                when(datasourceRegistryMock.isFact(DATASOURCE_1, true)).thenReturn(true);
                when(datasourceRegistryMock.containsDatasource(DATASOURCE_1)).thenReturn(true);
                when(dataSourceRepositoryMock.isAvailable(DATASOURCE_1)).thenReturn(true);
                when(datasourceRegistryMock.getJdbcDatasource(DATASOURCE_1)).thenReturn(JDBC_DATASOURCE_DUMMY);
                when(dataSourceRepositoryMock.doesTableContainData(
                        JDBC_DATASOURCE_DUMMY,
                        "table1",
                        testStart.toLocalDateTime(),
                        testEnd.toLocalDateTime()
                )).thenReturn(doesContainData);

                final boolean actual = objectUnderTests.isDataPresentForAnyDatasource(
                        testStart,
                        testEnd,
                        datasourceTables,
                        kpiDefinitions
                );

                verify(kpiDefinitionHelperMock).extractNonInMemoryDataSources(kpiDefinitions);
                verify(datasourceRegistryMock).isFact(DATASOURCE_1, true);
                verify(datasourceRegistryMock).containsDatasource(DATASOURCE_1);
                verify(dataSourceRepositoryMock).isAvailable(DATASOURCE_1);
                verify(datasourceRegistryMock).getJdbcDatasource(DATASOURCE_1);
                verify(dataSourceRepositoryMock).doesTableContainData(
                        JDBC_DATASOURCE_DUMMY,
                        "table1",
                        testStart.toLocalDateTime(),
                        testEnd.toLocalDateTime()
                );

                Assertions.assertThat(actual).isEqualTo(expected);
            }

        }
    }

    static class BlankStringsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(false, false),
                    Arguments.of(true, true)
            );
        }
    }
}