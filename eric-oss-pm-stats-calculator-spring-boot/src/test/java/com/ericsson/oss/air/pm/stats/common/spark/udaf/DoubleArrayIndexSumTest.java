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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.MutableAggregationBuffer;
import org.junit.jupiter.api.Test;
import scala.collection.JavaConverters;
import scala.collection.Seq;

class DoubleArrayIndexSumTest {

    @Test
    void whenArrayIsValid_ThenValidBufferReturned() {
        final DoubleArrayIndexSum result = new DoubleArrayIndexSum();
        result.initialize(buffer);
        assertThat(result.evaluate(buffer).toString()).hasToString("[3.5, 4.2, 1.5, 2.3, 3.5, 4.1]");
    }

    Seq<Double> convertListToSeq(final List<Double> inputList) {
        return JavaConverters.collectionAsScalaIterableConverter(inputList).asScala().toSeq();
    }

    MutableAggregationBuffer buffer = new MutableAggregationBuffer() {

        private static final long serialVersionUID = 1;

        @Override
        public int length() {
            return 1;
        }

        @Override
        public Object get(final int arg0) {
            final List<Double> list = new ArrayList<>();
            list.add(3.5);
            list.add(4.2);
            list.add(1.5);
            list.add(2.3);
            list.add(3.5);
            list.add(4.1);
            return convertListToSeq(list);
        }

        @Override
        public Row copy() {
            return null;
        }

        @Override
        public void update(final int arg0, final Object arg1) {

        }
    };
}