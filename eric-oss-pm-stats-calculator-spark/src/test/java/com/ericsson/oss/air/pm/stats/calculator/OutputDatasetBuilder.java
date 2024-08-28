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
 * Builds output {@link Dataset} needed to verify KPI calculations.
 */
class OutputDatasetBuilder {

    private OutputDatasetBuilder() {

    }

    static Dataset<Row> buildKpiAggElement1_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField percentileindex90Integer1440 = createStructField("percentileindex90_integer_1440", IntegerType, true);
        final StructField firstIntegerAggregateSlice1440 = createStructField("first_integer_aggregate_slice_1440", IntegerType, true);
        final StructField percentileindex80Integer1440 = createStructField("percentileindex80_integer_1440", IntegerType, true);
        final StructField transformIntArrayToFloatArray = createStructField("transform_int_array_to_float_array", ArrayType.apply(DoubleType), true);
        final StructField sumInteger1440 = createStructField("sum_integer_1440", IntegerType, true);
        final StructField maxLong1440 = createStructField("max_long_1440", LongType, true);
        final StructField firstTimestamp1440 = createStructField("first_timestamp_1440", TimestampType, true);
        final StructField sumIntegerArrayindex1440 = createStructField("sum_integer_arrayindex_1440", IntegerType, true);
        final StructField arrayindexsumIntegerarray1440 = createStructField("arrayindexsum_integerarray_1440", ArrayType.apply(IntegerType), true);
        final StructField firstString = createStructField("first_string", StringType, true);

        final StructType kpiAggElement1_1440Schema = DataTypes
                .createStructType(
                        new StructField[] { aggColumn0, aggregationBeginTime, aggregationEndTime,
                                percentileindex90Integer1440, firstIntegerAggregateSlice1440,
                                percentileindex80Integer1440,transformIntArrayToFloatArray, sumInteger1440,
                                maxLong1440, firstTimestamp1440, sumIntegerArrayindex1440,
                                arrayindexsumIntegerarray1440, firstString
                        });

        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement1_1440Schema), kpiAggElement1_1440Schema);
    }

    static Dataset<Row> buildSimpleKpiAggElement_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField sumInteger1440Simple = createStructField("sum_integer_1440_simple", IntegerType, true);
        final StructField sumIntegerArrayindex1440Simple = createStructField("sum_integer_arrayindex_1440_simple", IntegerType, true);

        final StructField firstCalculatePercentileValueSimple = createStructField("first_calculate_percentile_value_simple", DoubleType, true);
        final StructField firstCalculatePercentileBinSimple = createStructField("first_calculate_percentile_bin_simple", DoubleType, true);
        final StructField firstUpdateNullTimeAdvancedKpisSimple = createStructField("first_update_null_time_advanced_kpis_simple", ArrayType.apply(IntegerType), true);

        final StructField firstAddIntegerToArrayWithLimitSimple = createStructField("first_add_integer_to_array_with_limit_simple",  ArrayType.apply(IntegerType), true);

        final StructType kpiAggElement_1440Schema = DataTypes.createStructType(new StructField[]{
                aggColumn0, aggregationBeginTime, aggregationEndTime, sumInteger1440Simple,
                firstCalculatePercentileBinSimple, sumIntegerArrayindex1440Simple,
                firstCalculatePercentileValueSimple, firstUpdateNullTimeAdvancedKpisSimple, firstAddIntegerToArrayWithLimitSimple
        });

        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement_1440Schema), kpiAggElement_1440Schema);
        }

    static Dataset<Row> buildKpiAggElement2_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);
        final StructField aggColumn2 = createStructField("agg_column_2", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField sumReal1440 = createStructField("sum_real_1440", DoubleType, true);
        final StructField firstBoolean1440 = createStructField("first_boolean_1440", BooleanType, true);
        final StructField sumIntegerOperator1440 = createStructField("sum_integer_operator_1440", IntegerType, true);

        final StructType kpiAggElement1_1440Schema = DataTypes
                .createStructType(
                        new StructField[] { aggColumn0, aggColumn1, aggColumn2, aggregationBeginTime, aggregationEndTime, sumReal1440,
                                firstBoolean1440, sumIntegerOperator1440 });

        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement1_1440Schema), kpiAggElement1_1440Schema);
    }

    static Dataset<Row> buildSimpleKpiAggElement2_1440Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);
        final StructField aggColumn2 = createStructField("agg_column_2", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField sumFloat1440 = createStructField("sum_float_1440_simple", DoubleType, true);

        final StructType kpiAggElement1_1440Schema = DataTypes
                .createStructType(
                        new StructField[] { aggColumn0, aggColumn1, aggColumn2, aggregationBeginTime, aggregationEndTime, sumFloat1440 });

        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement1_1440Schema), kpiAggElement1_1440Schema);
    }

    static Dataset<Row> buildKpiAggElement1_60Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField firstIntegerOperator60Stage3 = createStructField("first_integer_operator_60_stage3", DoubleType, false);
        final StructField firstIntegerOperator60Stage4 = createStructField("first_integer_operator_60_stage4", DoubleType, false);
        final StructField firstIntegerOperator60Stage2 = createStructField("first_integer_operator_60_stage2", DoubleType, false);
        final StructField sumInteger60 = createStructField("sum_integer_60", IntegerType, false);
        final StructField sumIntegerCount60 = createStructField("sum_integer_count_60", IntegerType, false);
        final StructField arrayindexsumIntegerarray60 = createStructField("arrayindexsum_integerarray_60", ArrayType.apply(IntegerType), true);
        final StructField firstMedianFloat60 = createStructField("first_median_float_60", DoubleType, false);
        final StructField rollingCounterIntArray60 = createStructField("rolling_counter_int_array", ArrayType.apply(IntegerType), true);
        final StructField rollingDoubleArray60 = createStructField("rolling_double_array_60", ArrayType.apply(DoubleType), true);

        final StructType kpiAggElement1_1440Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0,
                        aggregationBeginTime,
                        firstMedianFloat60,
                        rollingCounterIntArray60,
                        rollingDoubleArray60,
                        arrayindexsumIntegerarray60,
                        sumInteger60,
                        sumIntegerCount60,
                        firstIntegerOperator60Stage3,
                        firstIntegerOperator60Stage2,
                        firstIntegerOperator60Stage4,
                        aggregationEndTime,
                });

        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement1_1440Schema), kpiAggElement1_1440Schema);
    }

    static Dataset<Row> buildKpiAggElement2_60Dataset(final SparkSession session, final Object[][] data) {
        final StructField aggColumn0 = createStructField("agg_column_0", LongType, false);
        final StructField aggColumn1 = createStructField("agg_column_1", LongType, false);
        final StructField aggColumn2 = createStructField("agg_column_2", LongType, false);
        final StructField aggregationBeginTime = createStructField("aggregation_begin_time", TimestampType, false);
        final StructField aggregationEndTime = createStructField("aggregation_end_time", TimestampType, false);
        final StructField firstIntegerDivideby060 = createStructField("first_float_divideby0_60", DoubleType, true);

        final StructType kpiAggElement1_1440Schema = DataTypes
                .createStructType(new StructField[] { aggColumn0, aggColumn1, aggColumn2, aggregationBeginTime, aggregationEndTime, firstIntegerDivideby060 });

        return session.createDataFrame(TestDatasetUtils.createListOfRows(data, kpiAggElement1_1440Schema), kpiAggElement1_1440Schema);
    }
}
