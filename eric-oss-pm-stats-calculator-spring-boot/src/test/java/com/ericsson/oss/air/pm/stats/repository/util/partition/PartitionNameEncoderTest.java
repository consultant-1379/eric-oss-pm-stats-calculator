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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PartitionNameEncoderTest {
    PartitionNameEncoder objectUnderTest = new PartitionNameEncoder();

    @Test
    void testEncodePartitionName() {
        final String partitionName = objectUnderTest.encodePartitionNameFromDate("table", LocalDate.parse("2024-03-04"));

        assertThat(partitionName).isEqualTo("table_p_2024_03_04");
    }

    @Test
    void testDecodePartitionName() {
        final LocalDate partitionDate = objectUnderTest.decodePartitionDateFromName("table_p_2024_03_04");

        assertThat(partitionDate).isEqualTo(LocalDate.parse("2024-03-04"));
    }

}