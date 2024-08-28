/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.api._spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import com.ericsson.oss.air.pm.stats._util.TestHelpers;
import com.ericsson.oss.air.pm.stats.calculator.api.model.kpi.definition.SchemaDetail;
import com.ericsson.oss.air.pm.stats.repository.api.SchemaDetailRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchemaDetailRepositoryTest {
    @Spy
    SchemaDetailRepository objectUnderTest;

    @Test
    void shouldFindAlreadySaved(@Mock final Connection connectionMock) throws SQLException {
        final SchemaDetail schemaDetail = TestHelpers.schemaDetail(1, "namespace", "topic");
        doReturn(Optional.of(schemaDetail)).when(objectUnderTest).findBy("topic", "namespace");

        final Integer actual = objectUnderTest.getOrSave(connectionMock, schemaDetail);

        verify(objectUnderTest).findBy("topic", "namespace");

        assertThat(actual).isOne();
    }

    @Test
    void shouldSaveNew(@Mock final Connection connectionMock) throws SQLException {
        final SchemaDetail schemaDetail = TestHelpers.schemaDetail(1, "namespace", "topic");
        doReturn(Optional.empty()).when(objectUnderTest).findBy("topic", "namespace");
        doReturn(1).when(objectUnderTest).save(connectionMock, schemaDetail);

        final Integer actual = objectUnderTest.getOrSave(connectionMock, schemaDetail);

        verify(objectUnderTest).findBy("topic", "namespace");
        verify(objectUnderTest).save(connectionMock, schemaDetail);

        assertThat(actual).isOne();
    }
}