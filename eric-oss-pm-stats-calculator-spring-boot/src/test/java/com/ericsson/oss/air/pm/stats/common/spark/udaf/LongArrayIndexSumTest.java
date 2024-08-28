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

class LongArrayIndexSumTest {

    @Test
    void whenArrayIsValid_ThenValidBufferReturned() {
        final LongArrayIndexSum result = new LongArrayIndexSum();
        result.initialize(buffer);
        assertThat(result.evaluate(buffer).toString()).hasToString("[3, 4, 1, 2, 3, 4]");
    }

    Seq<Long> convertListToSeq(final List<Long> inputList) {
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
            final List<Long> list = new ArrayList<>();
            list.add(3L);
            list.add(4L);
            list.add(1L);
            list.add(2L);
            list.add(3L);
            list.add(4L);
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