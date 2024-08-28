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

import static java.util.Collections.singletonList;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractDoubleArrayUdafTest {

    AbstractDoubleArrayUdaf objectUnderTest;

    @BeforeEach
    void setUp() {
        objectUnderTest = new AbstractDoubleArrayUdafTest.TestArrayUdaf(DataTypes.DoubleType);
    }

    @Test
    void shouldConstructObject() {
        final StructField expectedInputArray = DataTypes.createStructField("inputArray", DataTypes.createArrayType(DataTypes.DoubleType), true);
        Assertions.assertThat(objectUnderTest.inputSchema()).isEqualTo(DataTypes.createStructType(singletonList(expectedInputArray)));

        final StructField expectedBufferSchema = DataTypes.createStructField("bufferArray", DataTypes.createArrayType(DataTypes.DoubleType), true);
        Assertions.assertThat(objectUnderTest.bufferSchema()).isEqualTo(DataTypes.createStructType(singletonList(expectedBufferSchema)));

        Assertions.assertThat(objectUnderTest.dataType()).isEqualTo(DataTypes.DoubleType);
        Assertions.assertThat(objectUnderTest.deterministic()).isTrue();
    }

    @Test
    void shouldVerifySerialization() {
        final byte[] serialized = SerializationUtils.serialize(objectUnderTest);
        final AbstractDoubleArrayUdaf deserialized = SerializationUtils.deserialize(serialized);

        final StructField expectedInputArray = DataTypes.createStructField("inputArray", DataTypes.createArrayType(DataTypes.DoubleType), true);
        Assertions.assertThat(deserialized.inputSchema()).isEqualTo(DataTypes.createStructType(singletonList(expectedInputArray)));

        final StructField expectedBufferSchema = DataTypes.createStructField("bufferArray", DataTypes.createArrayType(DataTypes.DoubleType), true);
        Assertions.assertThat(deserialized.bufferSchema()).isEqualTo(DataTypes.createStructType(singletonList(expectedBufferSchema)));

        Assertions.assertThat(deserialized.dataType()).isEqualTo(DataTypes.DoubleType);
    }

    static final class TestArrayUdaf extends AbstractDoubleArrayUdaf {
        private TestArrayUdaf(final DataType returnDataType) {
            super(returnDataType);
        }

        @Override
        public Object evaluate(final Row buffer) {
            return null;
        }
    }
}