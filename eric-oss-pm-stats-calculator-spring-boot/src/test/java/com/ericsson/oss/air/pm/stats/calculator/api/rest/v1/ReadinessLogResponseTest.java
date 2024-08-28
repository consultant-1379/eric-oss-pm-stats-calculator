/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.api.rest.v1;

import java.time.LocalDateTime;
import java.time.Month;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ReadinessLogResponseTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerialize() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.of(2_022, Month.DECEMBER, 3, 7, 30);
        final String actual = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                ReadinessLogResponse.builder()
                        .withDatasource("datasource")
                        .withCollectedRowsCount(5L)
                        .withEarliestCollectedData(localDateTime)
                        .withLatestCollectedData(localDateTime)
                        .build()
        );

        Assertions.assertThat(actual).isEqualTo(String.join(System.lineSeparator(),
                "{",
                "  \"collectedRowsCount\" : 5,",
                "  \"datasource\" : \"datasource\",",
                "  \"earliestCollectedData\" : \"2022-12-03T07:30:00\",",
                "  \"latestCollectedData\" : \"2022-12-03T07:30:00\"",
                "}"
        ));
    }

    @Test
    void shouldDeserialize() throws JsonProcessingException {
        /* Deserialization is only needed to be able to deserialize returned objects in IT */

        final LocalDateTime localDateTime = LocalDateTime.of(2_022, Month.DECEMBER, 3, 7, 30);

        final ReadinessLogResponse actual = objectMapper.readValue(String.join(
                System.lineSeparator(),
                "{",
                "  \"collectedRowsCount\" : 5,",
                "  \"datasource\" : \"datasource\",",
                "  \"earliestCollectedData\" : \"2022-12-03T07:30:00\",",
                "  \"latestCollectedData\" : \"2022-12-03T07:30:00\"",
                "}"
        ), ReadinessLogResponse.class);

        Assertions.assertThat(actual).isEqualTo(
                ReadinessLogResponse.builder()
                        .withDatasource("datasource")
                        .withCollectedRowsCount(5L)
                        .withEarliestCollectedData(localDateTime)
                        .withLatestCollectedData(localDateTime)
                        .build()
        );
    }
}