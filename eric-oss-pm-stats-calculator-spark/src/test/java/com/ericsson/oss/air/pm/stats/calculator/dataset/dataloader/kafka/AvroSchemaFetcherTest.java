/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ericsson.oss.air.pm.stats.calculator.api.exception.SchemaRegistryException;
import com.ericsson.oss.air.pm.stats.calculator.configuration.property.CalculationProperties;
import com.ericsson.oss.air.pm.stats.calculator.dataset.dataloader.kafka.debug.SchemaLogger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.absa.abris.config.AbrisConfig;
import za.co.absa.abris.config.FromAvroConfig;
import za.co.absa.abris.config.FromConfluentAvroConfigFragment;
import za.co.absa.abris.config.FromSchemaDownloadingConfigFragment;
import za.co.absa.abris.config.FromStrategyConfigFragment;

@ExtendWith(MockitoExtension.class)
class AvroSchemaFetcherTest {

    private static final String SCHEMA_URL = "http://schemaregistry:8081";
    @Mock CalculationProperties calculationPropertiesMock;

    @Mock SchemaLogger schemaLoggerMock;

    @InjectMocks
    AvroSchemaFetcher objectUnderTest;

    private final String schemaName = "schema";
    private final String recordNamespace = "kpi-service";

    @Test
    public void shouldFetchAvroSchema() {
        testBase((final FromSchemaDownloadingConfigFragment fromSchemaDownloadingConfigFragmentMock) -> {
                final FromAvroConfig fromAvroConfigMock = mock(FromAvroConfig.class);
                when(fromSchemaDownloadingConfigFragmentMock.usingSchemaRegistry(SCHEMA_URL)).thenReturn(fromAvroConfigMock);
                objectUnderTest.fetchAvroSchema(schemaName, recordNamespace);
            });
    }

    @Test
    public void whenNullReturned_shouldThrowException() {
        testBase((final FromSchemaDownloadingConfigFragment fromSchemaDownloadingConfigFragmentMock) -> {
                when(fromSchemaDownloadingConfigFragmentMock.usingSchemaRegistry(SCHEMA_URL)).thenReturn(null);
                Assertions.assertThatThrownBy(() -> objectUnderTest.fetchAvroSchema(schemaName, recordNamespace))
                    .isInstanceOf(SchemaRegistryException.class)
                    .hasMessage("Failed to retrieve avro schema");
            });
    }

    @Test
    public void whenExceptionThrown_shouldThrowException() {
        testBase((final FromSchemaDownloadingConfigFragment fromSchemaDownloadingConfigFragmentMock) -> {
                when(fromSchemaDownloadingConfigFragmentMock.usingSchemaRegistry(SCHEMA_URL)).thenThrow(RuntimeException.class);
                Assertions.assertThatThrownBy(() -> objectUnderTest.fetchAvroSchema(schemaName, recordNamespace))
                    .isInstanceOf(SchemaRegistryException.class)
                    .hasMessage("Failed to retrieve avro schema");
            });
    }

    private void testBase(final AssertVisitor testMethod) {
        try (final MockedStatic<AbrisConfig> abrisConfigMockedStatic = mockStatic(AbrisConfig.class)) {
            final FromConfluentAvroConfigFragment fromConfluentAvroConfigFragmentMock = mock(FromConfluentAvroConfigFragment.class);
            final FromStrategyConfigFragment fromStrategyConfigFragmentMock = mock(FromStrategyConfigFragment.class);
            final FromSchemaDownloadingConfigFragment fromSchemaDownloadingConfigFragmentMock = mock(FromSchemaDownloadingConfigFragment.class);

            abrisConfigMockedStatic.when(AbrisConfig::fromConfluentAvro).thenReturn(fromConfluentAvroConfigFragmentMock);
            when(fromConfluentAvroConfigFragmentMock.downloadReaderSchemaByLatestVersion()).thenReturn(fromStrategyConfigFragmentMock);
            when(fromStrategyConfigFragmentMock.andRecordNameStrategy(schemaName, recordNamespace)).thenReturn(fromSchemaDownloadingConfigFragmentMock);
            when(calculationPropertiesMock.getSchemaRegistryUrl()).thenReturn(SCHEMA_URL);

            testMethod.execute(fromSchemaDownloadingConfigFragmentMock);

            abrisConfigMockedStatic.verify(AbrisConfig::fromConfluentAvro);
            verify(fromConfluentAvroConfigFragmentMock).downloadReaderSchemaByLatestVersion();
            verify(fromStrategyConfigFragmentMock).andRecordNameStrategy(schemaName, recordNamespace);
            verify(calculationPropertiesMock).getSchemaRegistryUrl();
            verify(fromSchemaDownloadingConfigFragmentMock).usingSchemaRegistry(SCHEMA_URL);
        }
    }

    @FunctionalInterface
    interface AssertVisitor {
        void execute(FromSchemaDownloadingConfigFragment fromSchemaDownloadingConfigFragmentMock);
    }
}