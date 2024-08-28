/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.calculator;

import static org.apache.spark.sql.types.DataTypes.BooleanType;
import static org.apache.spark.sql.types.DataTypes.DoubleType;
import static org.apache.spark.sql.types.DataTypes.IntegerType;
import static org.apache.spark.sql.types.DataTypes.LongType;
import static org.apache.spark.sql.types.DataTypes.StringType;
import static org.apache.spark.sql.types.DataTypes.TimestampType;
import static org.apache.spark.sql.types.DataTypes.createStructField;

import com.ericsson.oss.air.pm.stats.calculator.util.TestDatasetUtils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

/**
 * Builds input {@link Dataset}s needed to perform KPI calculation testing.
 */
class InputDatasetBuilder {

    private InputDatasetBuilder() {

    }

    static Dataset<Row> buildDimTable0Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, true);
        final StructField stringColumn0 = createStructField("stringColumn0", StringType, true);
        final StructField integerColumn0 = createStructField("integerColumn0", IntegerType, true);
        final StructField integerColumn1 = createStructField("integerColumn1", IntegerType, true);

        final StructType dimTable0Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, stringColumn0, integerColumn0, integerColumn1 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, dimTable0Schema), dimTable0Schema);
    }

    static Dataset<Row> buildDimTable1Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);
        final StructField aggColumn2 = createStructField("agg_column_2", LongType, false);
        final StructField booleanColumn0 = createStructField("booleanColumn0", BooleanType, true);

        final StructType dimTable1Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, aggColumn1, aggColumn2, booleanColumn0 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, dimTable1Schema), dimTable1Schema);
    }

    static Dataset<Row> buildDimTable2Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);

        final StructType dimTable2Schema = DataTypes.createStructType(new StructField[] { aggColumn0, aggColumn1 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, dimTable2Schema), dimTable2Schema);
    }

    static Dataset<Row> buildFactTable0Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField integerColumn0 = createStructField("integerColumn0", IntegerType, true);
        final StructField timestampColumn0 = createStructField("timestampColumn0", TimestampType, true);

        final StructType factTable0Schema = DataTypes.createStructType(new StructField[] { aggColumn0, aggregationBeginTime,
                integerColumn0, timestampColumn0 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, factTable0Schema), factTable0Schema);
    }

    static Dataset<Row> buildFactTable1Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, true);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, true);
        final StructField aggColumn2 = createStructField("agg_column_2", LongType, true);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, true);
        final StructField integerColumn0 = createStructField("integerColumn0", IntegerType, true);
        final StructField floatColumn0 = createStructField("floatColumn0", DoubleType, true);

        final StructType factTable1Schema = DataTypes.createStructType(new StructField[] { aggColumn0, aggColumn1, aggColumn2,
                aggregationBeginTime, integerColumn0, floatColumn0 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, factTable1Schema), factTable1Schema);
    }

    static Dataset<Row> buildFactTable2Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField integerArrayColumn0 = createStructField("integerArrayColumn0", ArrayType.apply(IntegerType), true);
        final StructField floatArrayColumn0 = createStructField("floatArrayColumn0", ArrayType.apply(DoubleType), true);
        final StructField integerColumn0 = createStructField("integerColumn0", IntegerType, true);
        final StructField floatColumn0 = createStructField("floatColumn0", DoubleType, true);
        final StructField integerArrayColumn1 = createStructField("integerArrayColumn1", ArrayType.apply(IntegerType), true);
        final StructField integerArrayColumn2 = createStructField("integerArrayColumn2", ArrayType.apply(IntegerType), true);
        final StructField floatArrayColumn1 = createStructField("floatArrayColumn1", ArrayType.apply(DoubleType), true);
        final StructField floatArrayColumn2 = createStructField("floatArrayColumn2", ArrayType.apply(DoubleType), true);

        final StructType factTable2Schema = DataTypes.createStructType(new StructField[]
                { aggColumn0, aggregationBeginTime, integerArrayColumn0, integerColumn0, floatColumn0,
                  floatArrayColumn0, integerArrayColumn1, integerArrayColumn2, floatArrayColumn1, floatArrayColumn2 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, factTable2Schema), factTable2Schema);
    }

    static Dataset<Row> buildFactTable3Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, true);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, true);
        final StructField aggColumn2 = createStructField("agg_column_2", LongType, true);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, true);
        final StructField integerColumn0 = createStructField("integerColumn0", IntegerType, true);
        final StructField integerArrayColumn0 = createStructField("integerArrayColumn0", ArrayType.apply(IntegerType), true);

        final StructType factTable3Schema = DataTypes.createStructType(new StructField[] { aggColumn0, aggColumn1, aggColumn2,
                aggregationBeginTime, integerColumn0, integerArrayColumn0 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, factTable3Schema), factTable3Schema);
    }

    static Dataset<Row> buildKpiAggElement1_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField percentileindex80Integer1440 = createStructField("percentileindex80_integer_1440", IntegerType, true);
        final StructField percentileindex90Integer1440 = createStructField("percentileindex90_integer_1440", IntegerType, true);
        final StructField arrayindexsumIntegerarray1440 = createStructField("arrayindexsum_integerarray_1440", ArrayType.apply(IntegerType), true);
        final StructField sumInteger1440 = createStructField("sum_integer_1440", IntegerType, true);
        final StructField sumIntegerArrayindex1440 = createStructField("sum_integer_arrayindex_1440", IntegerType, true);
        final StructField firstIntegerAggregateSlice1440 = createStructField("first_integer_aggregate_slice_1440", IntegerType, true);
        final StructField maxLong1440 = createStructField("max_long_1440", LongType, true);
        final StructField firstTimestamp1440 = createStructField("first_timestamp_1440", TimestampType, true);
        final StructField transformIntArrayToFloatArray = createStructField("transform_int_array_to_float_array", ArrayType.apply(DoubleType), true);
        final StructField firstString = createStructField("first_string", StringType, true);

        final StructType kpiAggElement1_1440Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, aggregationBeginTime, aggregationEndTime,
                        percentileindex80Integer1440,
                        percentileindex90Integer1440, arrayindexsumIntegerarray1440, sumInteger1440,
                        sumIntegerArrayindex1440, firstIntegerAggregateSlice1440, maxLong1440, firstTimestamp1440,
                        transformIntArrayToFloatArray, firstString });

        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement1_1440Schema), kpiAggElement1_1440Schema);
    }

    static Dataset<Row> buildSimpleKpiAggElement_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField sumInteger1440Simple = createStructField("sum_integer_1440_simple", IntegerType, true);
        final StructField sumIntegerArrayindex1440Simple = createStructField("sum_integer_arrayindex_1440_simple", IntegerType, true);

        final StructField firstCalculatePercentileValueSimple = createStructField("first_calculate_percentile_value_simple", DoubleType, true);
        final StructField firstCalculatePercentileBinSimple = createStructField("first_calculate_percentile_bin_simple", IntegerType, true);
        final StructField firstUpdateNullTimeAdvancedKpisSimple = createStructField("first_update_null_time_advanced_kpis_simple", ArrayType.apply(IntegerType), true);
        final StructField firstAddIntegerToArrayWithLimitSimple = createStructField("first_add_integer_to_array_with_limit_simple",  ArrayType.apply(IntegerType), true);

        final StructType simpleKpiAggElement_1440Schema = DataTypes.createStructType(new StructField[]{
                aggColumn0, aggregationBeginTime, aggregationEndTime, sumInteger1440Simple, sumIntegerArrayindex1440Simple,
                firstCalculatePercentileValueSimple, firstCalculatePercentileBinSimple, firstUpdateNullTimeAdvancedKpisSimple,
                firstAddIntegerToArrayWithLimitSimple
        });

        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, simpleKpiAggElement_1440Schema), simpleKpiAggElement_1440Schema);
    }

    static Dataset<Row> buildKpiAggElement2_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);
        final StructField aggColumn2 = createStructField("agg_column_2", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField firstBoolean1440 = createStructField("first_boolean_1440", BooleanType, true);
        final StructField sumIntegerOperator1440 = createStructField("sum_integer_operator_1440", IntegerType, true);
        final StructField sumReal1440 = createStructField("sum_real_1440", DoubleType, true);

        final StructType kpiAggElement2_1440Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, aggColumn1, aggColumn2, aggregationBeginTime, aggregationEndTime, firstBoolean1440,
                        sumIntegerOperator1440, sumReal1440,
                        });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement2_1440Schema), kpiAggElement2_1440Schema);
    }

    static Dataset<Row> buildSimpleKpiAggElement2_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);
        final StructField aggColumn2 = createStructField("agg_column_2", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        /*final StructField firstBoolean1440 = createStructField("first_boolean_1440", BooleanType, true);
        final StructField sumIntegerOperator1440 = createStructField("sum_integer_operator_1440", IntegerType, true);
        final StructField sumReal1440 = createStructField("sum_real_1440", DoubleType, true);*/
        final StructField sumFloat1440Simple = createStructField("sum_float_1440_simple", DoubleType, true);

        /*final StructType kpiAggElement2_1440Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, aggColumn1, aggColumn2, utcTimestamp, localTimestamp, firstBoolean1440,
                        firstFloatOperator1440Stage2, sumIntegerOperator1440, firstFloatGreatest1440Join, sumReal1440, sumFloat1440,
                        firstFloatLeast1440Join });*/
        final StructType kpiAggElement2_1440Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, aggColumn1, aggColumn2, aggregationBeginTime, aggregationEndTime, sumFloat1440Simple });

        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement2_1440Schema), kpiAggElement2_1440Schema);
    }

    static Dataset<Row> buildKpiAggElement3_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField firstInteger1440JoinKpidbFilter = createStructField("first_integer_1440_join_kpidb_filter", IntegerType, true);

        final StructType kpiAggElement3_1440Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, aggColumn1, aggregationBeginTime, aggregationEndTime,
                        firstInteger1440JoinKpidbFilter });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement3_1440Schema), kpiAggElement3_1440Schema);
    }

    static Dataset<Row> buildKpiAggElement1RollingAggregate_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField rollingSumInteger1440 = createStructField("rolling_sum_integer_1440", IntegerType, true);
        final StructField rollingMaxInteger1440 = createStructField("rolling_max_integer_1440", IntegerType, true);

        final StructType kpiAggElement1RollingAggregate_1440Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, aggregationBeginTime, aggregationEndTime, rollingSumInteger1440,
                        rollingMaxInteger1440 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement1RollingAggregate_1440Schema),
                kpiAggElement1RollingAggregate_1440Schema);
    }

    static Dataset<Row> buildKpiAggElement4ExecutionId_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField executionId = createStructField("execution_id", StringType, false);
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField executionidSumInteger1440 = createStructField("executionid_sum_integer_1440", IntegerType, true);

        final StructType kpiAggElement2_1440Schema = DataTypes
                .createStructType(new StructField[] { executionId, aggColumn0, aggregationBeginTime, aggregationEndTime, executionidSumInteger1440 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement2_1440Schema), kpiAggElement2_1440Schema);
    }

    static Dataset<Row> buildKpiAggElement1_60Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField sumInteger60 = createStructField("sum_integer_60", IntegerType, true);
        final StructField sumIntegerCount60 = createStructField("sum_integer_count_60", IntegerType, true);
        final StructField firstIntegerOperator60Stage3 = createStructField("first_integer_operator_60_stage3", DoubleType, true);
        final StructField firstIntegerOperator60Stage4 = createStructField("first_integer_operator_60_stage4", DoubleType, true);
        final StructField arrayindexsumIntegerarray60 = createStructField("arrayindexsum_integerarray_60", ArrayType.apply(IntegerType), true);
        final StructField firstIntegerOperator60Stage2 = createStructField("first_integer_operator_60_stage2", DoubleType, true);
        final StructField firstMedianFloat60 = createStructField("first_median_float_60", DoubleType, true);
        final StructField rollingCounterIntArray60 = createStructField("rolling_counter_int_array", ArrayType.apply(IntegerType), true);
        final StructField rollingDoubleArray60 = createStructField("rolling_double_array_60", ArrayType.apply(DoubleType), true);


        final StructType kpiAggElement1_60Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, aggregationBeginTime, aggregationEndTime, sumInteger60,
                        sumIntegerCount60, firstIntegerOperator60Stage3, firstIntegerOperator60Stage4,
                        arrayindexsumIntegerarray60, firstIntegerOperator60Stage2, firstMedianFloat60, rollingCounterIntArray60, rollingDoubleArray60 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement1_60Schema), kpiAggElement1_60Schema);
    }

    static Dataset<Row> buildKpiAggElement2_60Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);
        final StructField aggColumn2 = createStructField("agg_column_2", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField firstIntegerDivideby060 = createStructField("first_float_divideby0_60", DoubleType, true);

        final StructType kpiAggElement2_60Schema = DataTypes.createStructType(
                new StructField[] { aggColumn0, aggColumn1, aggColumn2, aggregationBeginTime, aggregationEndTime, firstIntegerDivideby060 });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement2_60Schema), kpiAggElement2_60Schema);
    }

    static Dataset<Row> buildKpiAggElement3_60Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField sumInteger60Join = createStructField("sum_integer_60_join", IntegerType, true);

        final StructType kpiAggElement3_60Schema = DataTypes.createStructType(
                new StructField[] { aggColumn0, aggColumn1, aggregationBeginTime, aggregationEndTime, sumInteger60Join });
        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement3_60Schema), kpiAggElement3_60Schema);
    }
}
