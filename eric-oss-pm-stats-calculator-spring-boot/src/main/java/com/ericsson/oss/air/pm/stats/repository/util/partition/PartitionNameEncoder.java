/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/


package com.ericsson.oss.air.pm.stats.repository.util.partition;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class PartitionNameEncoder {
    private static final String PARTITION_NAME_SEPARATOR = "_p_";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd", Locale.ENGLISH);

    public LocalDate decodePartitionDateFromName(final String partitionName) {
        final String date = StringUtils.splitByWholeSeparator(partitionName, PARTITION_NAME_SEPARATOR)[1];
        return LocalDate.parse(date, DATE_TIME_FORMATTER);
    }


    public String encodePartitionNameFromDate(final String tableName, final LocalDate partitionDate) {
        return tableName + PARTITION_NAME_SEPARATOR + partitionDate.format(DATE_TIME_FORMATTER);
    }
}
