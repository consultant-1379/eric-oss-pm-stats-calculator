/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import scala.collection.mutable.WrappedArray;

class OnConflictQueryBuilderTest {

    @Test
    void shouldHandlePartitionedTable() {
        final Map<String, Object> fieldsToUpdate = new LinkedHashMap<>(4);
        fieldsToUpdate.put("aggregation_begin_time", Timestamp.valueOf("2023-09-21 00:00:00.0"));
        fieldsToUpdate.put("aggregation_end_time", Timestamp.valueOf("2023-09-22 00:00:00.0"));
        fieldsToUpdate.put("nodeFDN", 44);
        fieldsToUpdate.put("sum_integer_arrayindex_1440_simple", 2);

        final List<String> primaryKeys = List.of("aggregation_begin_time", "nodeFDN");
        final List<String> updateColumns = List.of("aggregation_begin_time", "aggregation_end_time", "nodeFDN", "sum_integer_arrayindex_1440_simple");

        final String actual = OnConflictQueryBuilder.buildUpsertQuery("table", fieldsToUpdate, primaryKeys, updateColumns);
        Assertions.assertThat(actual).isEqualTo(
                "INSERT INTO table_p_2023_09_21(" +
                    "\"aggregation_begin_time\",\"aggregation_end_time\",\"nodeFDN\",\"sum_integer_arrayindex_1440_simple\"" +
                ") " +
                "VALUES ('2023-09-21 00:00:00.0','2023-09-22 00:00:00.0','44','2') " +
                "ON CONFLICT (\"aggregation_begin_time\",\"nodeFDN\") " +
                "DO UPDATE SET " +
                    "\"aggregation_begin_time\"='2023-09-21 00:00:00.0'," +
                    "\"aggregation_end_time\"='2023-09-22 00:00:00.0'," +
                    "\"nodeFDN\"='44'," +
                    "\"sum_integer_arrayindex_1440_simple\"='2';"
        );
    }

    @Test
    void whenValuesArePassedToTheOnConflictBuilder_thenTheCorrectQueryIsReturned() {
        final String expectedQuery = "INSERT INTO TestTable(\"columnOne\") VALUES ('testValue') "
                + "ON CONFLICT (\"columnOne\") DO UPDATE SET \"columnOne\"='testValue';";
        final List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("columnOne");
        final List<String> columnNames = new ArrayList<>();
        columnNames.add("columnOne");
        final Map<String, Object> fieldsToUpdate = new HashMap<>(1);
        fieldsToUpdate.put("columnOne", "testValue");
        assertEquals(expectedQuery, OnConflictQueryBuilder.buildUpsertQuery("TestTable", fieldsToUpdate, primaryKeys, columnNames));
    }

    @Test
    void whenValuesArePassedToTheOnConflictBuilder_andTheValuesContainAWrappedArray_thenTheCorrectQueryIsReturned() {
        final String expectedQuery = "INSERT INTO TestTable(\"columnTwo\") VALUES ('{A, B, C}') "
                + "ON CONFLICT (\"columnTwo\") DO UPDATE SET \"columnTwo\"='{A, B, C}';";
        final String[] testArray = { "A", "B", "C" };
        final WrappedArray<String> wrappedArray = WrappedArray.make(testArray);
        final List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("columnTwo");
        final List<String> columnNames = new ArrayList<>();
        columnNames.add("columnTwo");
        final Map<String, Object> fieldsToUpdate = new HashMap<>(1);
        fieldsToUpdate.put("columnTwo", wrappedArray);
        assertEquals(expectedQuery, OnConflictQueryBuilder.buildUpsertQuery("TestTable", fieldsToUpdate, primaryKeys, columnNames));
    }

    @Test
    void whenValuesArePassedToTheOnConflictBuilder_andTheValuesContainANullValue_thenTheCorrectQueryIsReturned() {
        final String expectedQuery = "INSERT INTO TestTable(\"columnThree\") VALUES (null) "
                + "ON CONFLICT (\"columnThree\") DO UPDATE SET \"columnThree\"=null;";
        final List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("columnThree");
        final List<String> columnNames = new ArrayList<>();
        columnNames.add("columnThree");
        final Map<String, Object> fieldsToUpdate = new HashMap<>(1);
        fieldsToUpdate.put("columnThree", null);
        assertEquals(expectedQuery, OnConflictQueryBuilder.buildUpsertQuery("TestTable", fieldsToUpdate, primaryKeys, columnNames));
    }
}
