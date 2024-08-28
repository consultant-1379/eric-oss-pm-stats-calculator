/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.StructType;

/**
 * Utils for input and output test datasets.
 */
public final class TestDatasetUtils {

    private TestDatasetUtils() {

    }

    public static List<Row> createListOfRows(final Object[][] data, final StructType schema) {
        final List<Row> rows = new ArrayList<>();
        for (final Object[] dataRow : data) {
            rows.add(new GenericRowWithSchema(dataRow, schema));
        }
        return rows;
    }
}
