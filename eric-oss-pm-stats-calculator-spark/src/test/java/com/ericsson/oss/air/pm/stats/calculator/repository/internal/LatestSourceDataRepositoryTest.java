/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.repository.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Optional;

import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.LatestSourceData;
import com.ericsson.oss.air.pm.stats.calculator.model.entity.internal.embedded.id.LatestSourceDataId;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test-containers")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class LatestSourceDataRepositoryTest {
    static final LocalDateTime TEST_TIME = LocalDateTime.of(LocalDate.of(2_022, Month.JULY, 19), LocalTime.NOON);

    @Autowired LatestSourceDataRepository objectUnderTest;

    @Test
    void shouldFindLastTimeCollected() {
        final LatestSourceDataId id =
                LatestSourceDataId.builder()
                                  .source("source")
                                  .executionGroup("executionGroup")
                                  .aggregationPeriodMinutes(1_440)
                                  .build();

        objectUnderTest.save(LatestSourceData.builder().id(id).latestTimeCollected(TEST_TIME).build());


        final Optional<LocalDateTime> actual =
                objectUnderTest.findLastTimeCollected(id.getSource(), id.getAggregationPeriodMinutes(), id.getExecutionGroup());

        Assertions.assertThat(actual).hasValue(TEST_TIME);
    }
}