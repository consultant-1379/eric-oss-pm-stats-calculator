/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PartitionUtilsTest {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final LocalDate START_DATE = LocalDate.of(2_022, Month.APRIL, 12);
    private static final LocalDate END_DATE = START_DATE.plusDays(10);

    private static final String PARTITION_NAME = "partitionName";
    private static final String TABLE_NAME = "tableName";

    private static final LinkedHashSet<String> UNIQUE_INDEX_COLUMNS = Sets.newLinkedHashSet("column1", "column2");

    private Partition partition;

    @BeforeEach
    void setUp() {
        partition = new Partition(PARTITION_NAME, TABLE_NAME, START_DATE, END_DATE, UNIQUE_INDEX_COLUMNS);
    }

    @Test
    void shouldCreatePartitionSql() {
        ;
        final String actual = PartitionUtils.createPartitionSql(partition);
        Assertions.assertThat(actual)
                .isEqualTo("CREATE TABLE IF NOT EXISTS %s PARTITION OF %s FOR VALUES FROM ('%s') TO ('%s');",
                        partition.getPartitionName(),
                        partition.getTableName(),
                        partition.getStartDate().format(DATE_TIME_FORMATTER),
                        partition.getEndDate().format(DATE_TIME_FORMATTER));
    }

    @Test
    void shouldCreatePartitionUniqueIndexName() {
        final String actual = PartitionUtils.createPartitionUniqueIndexName(partition);

        Assertions.assertThat(actual).isEqualTo("%s_ui", partition.getPartitionName());
    }

    @Test
    void whenGetPartitionDatesForRetentionIsCalled_thenCorrectDatesAreReturned() {
        final LocalDateTime nowDateTime = LocalDateTime.now();
        final List<LocalDate> actual = PartitionUtils.getPartitionDatesForRetentionPeriodAndCurrentDate(
                Duration.ofDays(3),
                nowDateTime
        );

        final LocalDate now = nowDateTime.toLocalDate();
        Assertions.assertThat(actual).containsExactly(
                now.minusDays(3),
                now.minusDays(2),
                now.minusDays(1),
                now,
                now.plusDays(1),
                now.plusDays(2)
        );
    }

    @Test
    void whenGetPartitionsIsCalled_thenCorrectPartitionsAreReturned() {
        final LocalDate now = LocalDate.of(2019, 5, 9);
        final List<Partition> actual = PartitionUtils.getPartitions(
                "table_1",
                "table_1",
                Arrays.asList(now, now.plusDays(1)),
                Set.of(
                        "aggregation_elelemnt_1",
                        "aggregation_elelemnt_2"
                )
        );

        Assertions.assertThat(actual).containsExactlyInAnyOrder(
                new Partition(
                        "table_1_p_2019_05_09",
                        "table_1",
                        now,
                        now.plusDays(1),
                        Set.of("aggregation_elelemnt_1", "aggregation_elelemnt_2")
                ),
                new Partition(
                        "table_1_p_2019_05_10",
                        "table_1",
                        now.plusDays(1),
                        now.plusDays(2),
                        Set.of("aggregation_elelemnt_1", "aggregation_elelemnt_2")
                )
        );
    }

    @Test
    void whenGetPartitionDateNextDayIsCalled_thenCorrectDateIsReturned() {
        final LocalDate nextDay = PartitionUtils.getPartitionDateNextDay(Clock.systemDefaultZone());
        final LocalDate now = LocalDate.now(); // Compared to current date used in testable code, current date here might be next day date.
        Assertions.assertThat(nextDay).isAfterOrEqualTo(now);
    }
}