/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.pm.stats.common.spark.udaf;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.MutableAggregationBuffer;
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

@Slf4j
@SuppressWarnings("deprecation")
abstract class AbstractDoubleArrayUdaf extends UserDefinedAggregateFunction {
    private static final long serialVersionUID = 3417451337989207462L;

    protected final StructType inputDataType;
    protected final StructType bufferSchema;
    @SuppressWarnings("squid:S1948")
    protected final DataType returnDataType;

    protected AbstractDoubleArrayUdaf(final DataType returnDataType) {
        super();
        final List<StructField> inputFields = new ArrayList<>(1);
        inputFields.add(DataTypes.createStructField("inputArray", DataTypes.createArrayType(DataTypes.DoubleType), true));
        inputDataType = DataTypes.createStructType(inputFields);

        final List<StructField> bufferFields = new ArrayList<>(1);
        bufferFields.add(DataTypes.createStructField("bufferArray", DataTypes.createArrayType(DataTypes.DoubleType), true));
        bufferSchema = DataTypes.createStructType(bufferFields);
        this.returnDataType = returnDataType;
    }

    @Override
    public StructType inputSchema() {
        return inputDataType;
    }

    @Override
    public StructType bufferSchema() {
        return bufferSchema;
    }

    @Override
    public DataType dataType() {
        return returnDataType;
    }

    @Override
    public boolean deterministic() {
        return true;
    }

    @Override
    public void initialize(final MutableAggregationBuffer buffer) {
        buffer.update(0, null);
    }

    @Override
    public void update(final MutableAggregationBuffer buffer, final Row input) {
        // We only update the buffer when the input value is not null.
        if (!input.isNullAt(0)) {
            if (buffer.isNullAt(0)) {
                // If the buffer value (the intermediate result of the sum) is still null,
                // we set the input value to the buffer.
                buffer.update(0, input.getList(0));
            } else {
                // Otherwise, we add the input value to the buffer value.
                // Need to new ArrayList first, or convert to scala collection unable to set value.
                final List<Double> newValue = new ArrayList<>(convertNullValuesToZero(input.getList(0)));
                final List<Double> iniVal = new ArrayList<>(convertNullValuesToZero(buffer.getList(0)));

                if (newValue.size() == iniVal.size()) {
                    for (int i = 0; i < newValue.size(); i++) {
                        newValue.set(i, convertNullToZero(newValue.get(i)) + convertNullToZero(iniVal.get(i)));
                    }
                    buffer.update(0, newValue);
                } else {
                    log.warn("Attempted to aggregate arrays of different sizes from source data. It is not possible to " +
                             "determine how to aggregate these correctly, so the current value will not be updated. Please ensure " +
                             "consistency in the size of arrays from the source data.");
                    buffer.update(0, iniVal);
                }
            }
        }
    }

    private static List<Double> convertNullValuesToZero(final List<Double> input) {
        final List<Double> nonNullValues = new ArrayList<>(input.size());

        for (final Double d : input) {
            final double valueToAdd = d == null ? 0.0 : d;
            nonNullValues.add(valueToAdd);
        }

        return nonNullValues;
    }

    @Override
    public void merge(final MutableAggregationBuffer buffer1, final Row buffer2) {
        // We only update the buffer1 when the input buffer2's value is not null.
        if (!buffer2.isNullAt(0)) {
            if (buffer1.isNullAt(0)) {
                // If the buffer value (intermediate result of the sum) is still null, we set the it as the input buffer's value.
                buffer1.update(0, buffer2.getList(0));

            } else {
                // Otherwise, we add the input value to the buffer value.
                final List<Double> newValue = new ArrayList<>(buffer2.getList(0));
                final List<Double> iniVal = new ArrayList<>(buffer1.getList(0));

                if (newValue.size() == iniVal.size()) {
                    for (int i = 0; i < newValue.size(); i++) {
                        newValue.set(i, convertNullToZero(newValue.get(i)) + convertNullToZero(iniVal.get(i)));
                    }
                    buffer1.update(0, newValue);
                } else {
                    log.warn("Attempted to aggregate arrays of different sizes from source data. It is not possible to " +
                             "determine how to aggregate these correctly, so the current value will not be updated. Please ensure " +
                             "consistency in the size of arrays from the source data.");
                    buffer1.update(0, iniVal);
                }
            }
        }
    }

    private static Double convertNullToZero(final Double value) {
        return value == null ? Double.valueOf(0) : value;
    }

    @Override
    public abstract Object evaluate(Row buffer);
}