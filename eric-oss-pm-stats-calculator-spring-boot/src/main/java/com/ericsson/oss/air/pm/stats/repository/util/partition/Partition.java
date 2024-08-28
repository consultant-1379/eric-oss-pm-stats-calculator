/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.partition;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class Partition {

    private String partitionName;
    private String tableName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<String> uniqueIndexColumns;

    public Partition(final String partitionName, final String tableName, final LocalDate startDate, final LocalDate endDate,
                     final Set<String> uniqueIndexColumns) {
        this.partitionName = partitionName;
        this.tableName = tableName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.uniqueIndexColumns = uniqueIndexColumns == null ? new HashSet<>() : new HashSet<>(uniqueIndexColumns);
    }

    public Set<String> getUniqueIndexColumns() {
        return new HashSet<>(uniqueIndexColumns);
    }

}
