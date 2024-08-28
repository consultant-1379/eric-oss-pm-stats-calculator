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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.oss.air.pm.stats.repository.util.partition.Partition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PartitionUtils {
    public static final String PARTITION_NAME_TABLE_NAME_AND_DATE_SEPARATOR = "_p_";
    public static final String PARTITION_NAME_DATE_PATTERN = "yyyy_MM_dd";
    public static final String PARTITION_UNIQUE_INDEX_SUFFIX = "_ui";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);


    /**
     * SQL for partition creation.
     *
     * @param partition {@link Partition} to generate SQL for
     * @return partition creation SQL
     */
    public static String createPartitionSql(final Partition partition) {
        return String.format("CREATE TABLE IF NOT EXISTS %s PARTITION OF %s FOR VALUES FROM ('%s') TO ('%s');",
                partition.getPartitionName(),
                partition.getTableName(),
                partition.getStartDate().format(DATE_TIME_FORMATTER),
                partition.getEndDate().format(DATE_TIME_FORMATTER));
    }

    /**
     * To create unique index name.
     *
     * @param partition {@link Partition} for which unique index name to return
     * @return unique index name
     */
    public static String createPartitionUniqueIndexName(final Partition partition) {
        return partition.getPartitionName() + PARTITION_UNIQUE_INDEX_SUFFIX;
    }

    /**
     * Get the Partition Dates for given Retention Days.
     *
     * @param retentionPeriod {@link Duration} of the retention period
     * @return {@link List} of {@link LocalDate} - List of dates to create db partitions
     */
    public static List<LocalDate> getPartitionDatesForRetention(@NonNull final Duration retentionPeriod) {
        final LocalDateTime localDateTime = LocalDateTime.now();
        return getPartitionDatesForRetentionPeriodAndCurrentDate(retentionPeriod, localDateTime);
    }

    /**
     * Get the Partition Dates for given Retention Days and current data.
     *
     * @param retentionPeriod {@link Duration} of the retention period
     * @param localDateTime   {@link LocalDateTime} to be used for getting partition dates
     * @return {@link List} of {@link LocalDate} - List of dates to create db partitions
     */
    public static List<LocalDate> getPartitionDatesForRetentionPeriodAndCurrentDate(@NonNull final Duration retentionPeriod,
                                                                                    @NonNull final LocalDateTime localDateTime) {
        final LocalDateTime dateMinusLookUpPeriod = localDateTime.minusDays(retentionPeriod.toDays());
        return getDates(dateMinusLookUpPeriod.toLocalDate().atStartOfDay(), localDateTime.plusDays(3));
    }

    private static List<LocalDate> getDates(final LocalDateTime startDate, final LocalDateTime endDate) {
        final long numOfDays = ChronoUnit.DAYS.between(startDate, endDate);
        return Stream.iterate(startDate.toLocalDate(), date -> date.plusDays(1)).limit(numOfDays).collect(Collectors.toList());
    }

    /**
     * To get {@link List} of {@link Partition} for given dates and table name.
     *
     * @param partitionNamePrefix prefix to be used for partitions name
     * @param tableName           table name to get partitions for
     * @param partitionDates      {@link List} of {@link LocalDate} for which partition to be created
     * @param uniqueIndexColumns  columns for partition unique index
     * @return {@link List} of {@link Partition}
     */
    public static List<Partition> getPartitions(final String partitionNamePrefix, final String tableName, final List<LocalDate> partitionDates,
                                                final Set<String> uniqueIndexColumns) {
        final List<Partition> partitions = new ArrayList<>(partitionDates.size());

        for (final LocalDate partitionStartDate : partitionDates) {
            final String partitionName = createPartitionName(partitionNamePrefix, partitionStartDate);
            final Partition partition = new Partition(partitionName, tableName, partitionStartDate, partitionStartDate.plusDays(1),
                    uniqueIndexColumns);
            partitions.add(partition);
        }
        return partitions;
    }

    private static String createPartitionName(final String partitionNamePrefix, final LocalDate partitionStartDate) {
        return partitionNamePrefix.concat(PARTITION_NAME_TABLE_NAME_AND_DATE_SEPARATOR)
                .concat(partitionStartDate.format(DateTimeFormatter.ofPattern(PARTITION_NAME_DATE_PATTERN)));
    }

    /**
     * Get the Partition Date for next Day.
     *
     * @return {@link LocalDate} - Date to create db partition of
     */
    public static LocalDate getPartitionDateNextDay(Clock clock) {
        return LocalDateTime.now(clock).toLocalDate().plusDays(1L);
    }
}
