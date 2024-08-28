/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.configuration.rest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.calculator.configuration.CalculatorProperties;
import com.ericsson.oss.air.pm.stats.configuration.rest.DataCatalogWebClientProducer.DataCatalogWebClient;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataCatalogWebClientProducerTest {
    @Mock CalculatorProperties calculatorPropertiesMock;

    @InjectMocks DataCatalogWebClientProducer objectUnderTest;

    @Test
    void shouldCreateDataCatalogWebClient() {
        when(calculatorPropertiesMock.getDataCatalogUrl()).thenReturn("localhost:8080");

        final DataCatalogWebClient actual = objectUnderTest.dataCatalogWebClient();

        verify(calculatorPropertiesMock).getDataCatalogUrl();

        Assertions.assertThat(actual).isNotNull();
    }
}