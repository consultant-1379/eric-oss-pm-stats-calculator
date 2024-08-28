/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.repository.util.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.air.pm.stats.common.model.collection.Column;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
public class PartitionUniqueIndex {
    private String partitionName;
    private String uniqueIndexName;
    @With
    private List<Column> indexes;

    public PartitionUniqueIndex(final String partitionName, final String uniqueIndexName, final List<Column> indexes) {
        this.partitionName = partitionName;
        this.uniqueIndexName = uniqueIndexName;
        this.indexes = indexes == null ? new ArrayList<>() : new ArrayList<>(indexes);
    }

    public List<Column> indexColumns() {
        return Collections.unmodifiableList(indexes);
    }


    public List<Column> indexColumnsWithTrimmedQuotes() {
        final String quote = "\"";
        final List<Column> trimmedColumns = new ArrayList<>();

        indexes.forEach(column -> {
            final String columnName = column.getName();
            if (columnName.startsWith(quote) && columnName.endsWith(quote)) {
                trimmedColumns.add(Column.of(columnName.substring(1, columnName.length() - 1)));
            } else {
                trimmedColumns.add(column);
            }
        });

        return trimmedColumns;
    }


}
