/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.Filter;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.KpiDefinition;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.filter.loader.PostgresCustomFilterDataLoaderImpl;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.postgres.iterator.factory.FilterIteratorFactoryImpl;
import com.ericsson.oss.air.pm.stats.calculator.service.api.KpiDefinitionService;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataLoaderPostgresTest {
    @Mock PostgresCustomFilterDataLoaderImpl postgresCustomFilterDataLoaderMock;
    @Mock FilterIteratorFactoryImpl filterIteratorFactoryMock;
    @Mock KpiDefinitionService kpiDefinitionServiceMock;

    @InjectMocks DataLoaderPostgres objectUnderTest;

    @Test
    void shouldIterateDefaultFilter(@Mock final Collection<KpiDefinition> kpiDefinitionsMock) {
        objectUnderTest.iteratorDefaultFilter(kpiDefinitionsMock);

        verify(filterIteratorFactoryMock).createDefault(kpiDefinitionsMock);
    }

    @Test
    void shouldLoadCustomFilter(@Mock final Collection<KpiDefinition> kpiDefinitionsMock) {
        final Set<Filter> filters = Collections.emptySet();

        objectUnderTest.loadDatasetsWithCustomFilter(filters, kpiDefinitionsMock);

        verify(postgresCustomFilterDataLoaderMock).loadDatasetsWithCustomFilter(kpiDefinitionsMock, filters);
    }

    @Nested
    class VerifySupport {
        @Mock Collection<KpiDefinition> kpiDefinitionsMock;

        @Test
        void shouldSupport() {
            when(kpiDefinitionServiceMock.areNonScheduledSimple(kpiDefinitionsMock)).thenReturn(true);

            final boolean actual = objectUnderTest.supports(kpiDefinitionsMock);

            verify(kpiDefinitionServiceMock).areNonScheduledSimple(kpiDefinitionsMock);

            assertThat(actual).isTrue();
        }

        @Test
        void shouldNotSupport() {
            when(kpiDefinitionServiceMock.areNonScheduledSimple(kpiDefinitionsMock)).thenReturn(false);

            final boolean actual = objectUnderTest.supports(kpiDefinitionsMock);

            verify(kpiDefinitionServiceMock).areNonScheduledSimple(kpiDefinitionsMock);

            assertThat(actual).isFalse();
        }
    }
}