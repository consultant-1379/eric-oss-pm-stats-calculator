/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.service.validator.retention;

import java.time.Duration;

import com.ericsson.oss.air.pm.stats.configuration.environment.model.EnvironmentValue;
import com.ericsson.oss.air.pm.stats.repository.api.CollectionRetentionRepository;
import com.ericsson.oss.air.pm.stats.repository.api.TableRetentionRepository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetentionPeriodManagerTest {
    @Mock
    TableRetentionRepository tableRetentionRepositoryMock;
    @Mock
    CollectionRetentionRepository collectionRetentionRepositoryMock;

    @Mock
    EnvironmentValue<Duration> retentionPeriodDaysMock;

    @InjectMocks
    RetentionPeriodManager objectUnderTest;

    @Test
    void shouldCreateRetentionPeriodMemoizer() {
        final RetentionPeriodMemoizer actual = objectUnderTest.retentionPeriodMemoizer();
        Assertions.assertThat(actual).isNotNull();
    }
}