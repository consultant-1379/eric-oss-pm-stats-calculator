/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.air.pm.stats.cache.SchemaDetailCache;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.DataIdentifier;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;

import kpi.model.simple.table.definition.optional.SimpleDefinitionInpDataIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaSubjectHelperTest {

    @Mock
    SchemaDetailCache schemaDetailCacheMock;

    @InjectMocks
    SchemaSubjectHelper objectUnderTest;

    SimpleDefinitionInpDataIdentifier testDataIdentifier = SimpleDefinitionInpDataIdentifier.of("testDataSpace|testCategory|testSchema");

    @Test
    void shouldReturnSubjectOldModel() {
        final DataIdentifier dataIdentifier = DataIdentifier.of("something|something|schema");

        when(schemaDetailCacheMock.get(dataIdentifier)).thenReturn(SchemaDetail.builder().withNamespace("namespace").build());

        final String actual = objectUnderTest.createSubjectRepresentation(dataIdentifier);

        verify(schemaDetailCacheMock).get(dataIdentifier);

        assertThat(actual).isEqualTo("namespace.schema");
    }

    @Test
    void shouldReturnSubject() {
        when(schemaDetailCacheMock.get(testDataIdentifier)).thenReturn(SchemaDetail.builder().withNamespace("testNamespace").build());

        final String actual = objectUnderTest.createSubjectRepresentation(testDataIdentifier);

        verify(schemaDetailCacheMock).get(testDataIdentifier);

        assertThat(actual).isEqualTo("testNamespace.testSchema");
    }
}